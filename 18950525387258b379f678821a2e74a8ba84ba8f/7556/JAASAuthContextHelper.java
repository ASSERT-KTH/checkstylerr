/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.jaspic.config.jaas;

import com.sun.jaspic.config.helper.AuthContextHelper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;

/**
 *
 * @author Ron Monzillo
 */
public class JAASAuthContextHelper extends AuthContextHelper {

    private static final String DEFAULT_ENTRY_NAME = "other";
    private static final Class[] PARAMS = {};
    private static final Object[] ARGS = {};
    //may be more than one delegate for a given jaas config file
    private ReentrantReadWriteLock instanceReadWriteLock =
            new ReentrantReadWriteLock();
    private Lock instanceWriteLock = instanceReadWriteLock.writeLock();
    ExtendedConfigFile jaasConfig;
    private final String appContext;
    private AppConfigurationEntry[] entry;
    private Constructor[] ctor;

    public JAASAuthContextHelper(String loggerName, boolean returnNullContexts,
            ExtendedConfigFile jaasConfig, Map properties, String appContext)
            throws AuthException {
        super(loggerName, returnNullContexts);
        this.jaasConfig = jaasConfig;
        this.appContext = appContext;
        initialize();
    }

    private void initialize() {
        boolean found = false;
        boolean foundDefault = false;
        instanceWriteLock.lock();
        try {
            entry = jaasConfig.getAppConfigurationEntry(appContext);
            if (entry == null) {
                // NEED TO MAKE SURE THIS LOOKUP only occurs when registered for *
                entry = jaasConfig.getAppConfigurationEntry(DEFAULT_ENTRY_NAME);
                if (entry == null) {
                    entry = new AppConfigurationEntry[0];
                } else {
                    foundDefault = true;
                }
            } else {
                found = true;
            }
            // initializeContextMap();
            ctor = null;
        } finally {
            instanceWriteLock.unlock();
        }
        if (!found) {
            if (!foundDefault) {
                logIfLevel(Level.INFO, null,
                        "JAASAuthConfig no entries matched appContext (",
                        appContext, ") or (", DEFAULT_ENTRY_NAME, ")");
            } else {
                logIfLevel(Level.INFO, null,
                        "JAASAuthConfig appContext (", appContext, ") matched (",
                        DEFAULT_ENTRY_NAME, ")");
            }
        }
    }

    private <M> void loadConstructors(M[] template, String authContextID) throws AuthException {
        if (ctor == null) {
            try {
                final Class moduleType = template.getClass().getComponentType();
                ctor = (Constructor[]) AccessController.doPrivileged(
                        new java.security.PrivilegedExceptionAction() {

                            public Object run() throws
                                    java.lang.ClassNotFoundException,
                                    java.lang.NoSuchMethodException,
                                    java.lang.InstantiationException,
                                    java.lang.IllegalAccessException,
                                    java.lang.reflect.InvocationTargetException {
                                Constructor[] ctor = new Constructor[entry.length];
                                ClassLoader loader =
                                        Thread.currentThread().getContextClassLoader();
                                for (int i = 0; i < entry.length; i++) {
                                    ctor[i] = null;
                                    String clazz = entry[i].getLoginModuleName();
                                    try {
                                        Class c = Class.forName(clazz, true, loader);
                                        if (moduleType.isAssignableFrom(c)) {
                                            ctor[i] = c.getConstructor(PARAMS);
                                        }

                                    } catch (Throwable t) {
                                        logIfLevel(Level.WARNING, null,
                                                "skipping unloadable class: ",
                                                clazz, " of appCOntext: ", appContext);
                                    }
                                }
                                return ctor;
                            }
                        });
            } catch (java.security.PrivilegedActionException pae) {
                AuthException ae = new AuthException();
                ae.initCause(pae.getCause());
                throw ae;
            }
        }
    }

    protected final void refresh() {
        jaasConfig.refresh();
        initialize();
    }

    /**
     * this implementation does not depend on authContextID
     * @param <M>
     * @param template
     * @param authContextID (ignored by this context system)
     * @return
     * @throws AuthException
     */
    public <M> boolean hasModules(M[] template, String authContextID) throws AuthException {
        loadConstructors(template, authContextID);
        for (Constructor c : ctor) {
            if (c != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * this implementation does not depend on authContextID
     * @param <M>
     * @param template
     * @param authContextID (ignored by this context system)
     * @return
     * @throws AuthException
     */
    public <M> M[] getModules(M[] template, String authContextID) throws AuthException {
        loadConstructors(template, authContextID);
        ArrayList<M> list = new ArrayList<M>();
        for (int i = 0; i < ctor.length; i++) {
            if (ctor[i] == null) {
                list.add(i, null);
            } else {
                final int j = i;
                try {
                    list.add(j, AccessController.doPrivileged(
                            new java.security.PrivilegedExceptionAction<M>() {

                                public M run()
                                        throws InstantiationException,
                                        IllegalAccessException,
                                        IllegalArgumentException,
                                        InvocationTargetException {
                                    return (M) ctor[j].newInstance(ARGS);
                                }
                            }));
                } catch (PrivilegedActionException pae) {
                    AuthException ae = new AuthException();
                    ae.initCause(pae.getCause());
                    throw ae;
                }
            }
        }
        return list.toArray(template);
    }

    public Map<String, ?> getInitProperties(int i, Map<String, ?> properties) {
        Map<String, Object> rvalue = new HashMap<String, Object>();
        if (entry[i] != null) {
            if (properties != null && !properties.isEmpty()) {
                rvalue.putAll((Map<String, Object>) properties);
            }
            Map<String, Object> options = (Map<String, Object>) entry[i].getOptions();
            if (options != null && !options.isEmpty()) {
                rvalue.putAll(options);
            }
        }
        return rvalue;
    }

    public boolean exitContext(AuthStatus[] successValue, int i, AuthStatus moduleStatus) {
        if (entry[i] != null && ctor[i] != null) {
            LoginModuleControlFlag flag = entry[i].getControlFlag();
            if (LoginModuleControlFlag.REQUISITE.equals(flag)) {
                for (AuthStatus s : successValue) {
                    if (moduleStatus == s) {
                        return false;
                    }
                }
                return true;
            } else if (LoginModuleControlFlag.SUFFICIENT.equals(flag)) {
                for (AuthStatus s : successValue) {
                    if (moduleStatus == s) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public AuthStatus getReturnStatus(AuthStatus[] successValue,
            AuthStatus defaultFailStatus,
            AuthStatus[] status,
            int position) {
        AuthStatus result = null;
        for (int i = 0; i <= position; i++) {
            if (entry[i] != null && ctor[i] != null) {
                LoginModuleControlFlag flag = entry[i].getControlFlag();
                if (isLoggable(Level.FINE)) {
                    logIfLevel(Level.FINE, null, "getReturnStatus - flag: ",
                            flag.toString());
                }
                if (flag == LoginModuleControlFlag.REQUIRED
                        || flag == LoginModuleControlFlag.REQUISITE) {
                    boolean isSuccessValue = false;
                    for (AuthStatus s : successValue) {
                        if (status[i] == s) {
                            isSuccessValue = true;
                        }
                    }
                    if (isSuccessValue) {
                        if (result == null) {
                            result = status[i];
                        }
                        continue;
                    }
                    if (isLoggable(Level.FINE)) {
                        logIfLevel(Level.FINE, null, "ReturnStatus - REQUIRED or REQUISITE failure: ",
                                status[i].toString());
                    }
                    return status[i];
                } else if (flag == LoginModuleControlFlag.SUFFICIENT) {
                    if (exitContext(successValue, i, status[i])) {
                        if (isLoggable(Level.FINE)) {
                            logIfLevel(Level.FINE, null, "ReturnStatus - Sufficient success: ",
                                    status[i].toString());
                        }
                        return status[i];
                    }

                } else if (flag == LoginModuleControlFlag.OPTIONAL) {
                    if (result == null) {
                        for (AuthStatus s : successValue) {
                            if (status[i] == s) {
                                result = status[i];
                            }
                        }
                    }
                }
            }
        }
        if (result != null) {
            if (isLoggable(Level.FINE)) {
                logIfLevel(Level.FINE, null, "ReturnStatus - result: ", result.toString());
            }
            return result;
        }
        if (isLoggable(Level.FINE)) {
            logIfLevel(Level.FINE, null, "ReturnStatus - Default faiure status: ",
                    defaultFailStatus.toString());
        }
        return defaultFailStatus;
    }
}
