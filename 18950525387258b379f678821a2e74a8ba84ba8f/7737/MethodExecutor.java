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

package com.sun.gjc.util;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Vector;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Execute the methods based on the parameters.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/23
 */
public class MethodExecutor implements java.io.Serializable {

    private static Logger _logger;

    static {
        _logger = LogDomains.getLogger(MethodExecutor.class, LogDomains.RSR_LOGGER);
    }

    private boolean debug = false;

    private final static String newline = System.getProperty("line.separator");

    private static StringManager sm = StringManager.getManager(
            DataSourceObjectBuilder.class);

    /**
     * Exceute a simple set Method.
     *
     * @param value  Value to be set.
     * @param method <code>Method</code> object.
     * @param obj    Object on which the method to be executed.
     * @throws <code>ResourceException</code>,
     *          in case of the mismatch of parameter values or
     *          a security violation.
     */
    public void runJavaBeanMethod(String value, Method method, Object obj) throws ResourceException {
        if (value == null || value.trim().equals("")) {
            return;
        }

        Class[] parameters = method.getParameterTypes();
        if (parameters.length == 1) {
            Object[] values = new Object[1];
            values[0] = convertType(parameters[0], value);

            final ResourceException[] exception = new ResourceException[1];
            AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            method.setAccessible(true);
                            method.invoke(obj, values);
                        } catch (IllegalAccessException | InvocationTargetException | SecurityException iae) {
                            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", value);
                            _logger.log(Level.SEVERE, "", iae);
                            String msg = sm.getString("me.access_denied",
                                method.getName());
                            exception[0] = new ResourceException(msg);
                        } catch (IllegalArgumentException ie) {
                            _logger.log(Level.SEVERE, "jdbc.exc_jb_val", value);
                            _logger.log(Level.SEVERE, "", ie);
                            String msg = sm
                                .getString("me.illegal_args", method.getName());
                            exception[0] = new ResourceException(msg);
                        }
                        return null;
                    }
                });
            if( exception[0] != null){
                throw exception[0];
            }
        }
    }

    /**
     * Executes the method.
     *
     * @param method <code>Method</code> object.
     * @param obj    Object on which the method to be executed.
     * @param values Parameter values for executing the method.
     * @throws <code>ResourceException</code>,
     *          in case of the mismatch of parameter values or
     *          a security violation.
     */
    public void runMethod(Method method, Object obj, Vector values) throws ResourceException {
            Class[] parameters = method.getParameterTypes();
            if (values.size() != parameters.length) {
                return;
            }
            Object[] actualValues = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                String val = (String) values.get(i);
                if (val.trim().equals("NULL")) {
                    actualValues[i] = null;
                } else {
                    actualValues[i] = convertType(parameters[i], val);
                }
            }
        final ResourceException[] exception = new ResourceException[1];
         AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    method.setAccessible(true);
                    method.invoke(obj, actualValues);
                } catch (IllegalAccessException | InvocationTargetException | SecurityException iae) {
                    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", values);
                    _logger.log(Level.SEVERE, "", iae);
                    String msg = sm
                        .getString("me.access_denied", method.getName());
                    exception[0] = new ResourceException(msg);
                } catch (IllegalArgumentException ie) {
                    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", values);
                    _logger.log(Level.SEVERE, "", ie);
                    String msg = sm
                        .getString("me.illegal_args", method.getName());
                    exception[0] = new ResourceException(msg);
                }
                return null;
            }
        });
        if( exception[0] != null){
            throw exception[0];
        }
    }

    /**
     * Converts the type from String to the Class type.
     *
     * @param type      Class name to which the conversion is required.
     * @param parameter String value to be converted.
     * @return Converted value.
     * @throws <code>ResourceException</code>,
     *          in case of the mismatch of parameter values or
     *          a security violation.
     */
    private Object convertType(Class type, String parameter) throws ResourceException {
        try {
            String typeName = type.getName();
            if (typeName.equals("java.lang.String") || typeName.equals("java.lang.Object")) {
                return parameter;
            }

            if (typeName.equals("int") || typeName.equals("java.lang.Integer")) {
                return Integer.valueOf(parameter);
            }

            if (typeName.equals("short") || typeName.equals("java.lang.Short")) {
                return Short.valueOf(parameter);
            }

            if (typeName.equals("byte") || typeName.equals("java.lang.Byte")) {
                return Byte.valueOf(parameter);
            }

            if (typeName.equals("long") || typeName.equals("java.lang.Long")) {
                return Long.valueOf(parameter);
            }

            if (typeName.equals("float") || typeName.equals("java.lang.Float")) {
                return Float.valueOf(parameter);
            }

            if (typeName.equals("double") || typeName.equals("java.lang.Double")) {
                return Double.valueOf(parameter);
            }

            if (typeName.equals("java.math.BigDecimal")) {
                return new java.math.BigDecimal(parameter);
            }

            if (typeName.equals("java.math.BigInteger")) {
                return new java.math.BigInteger(parameter);
            }

            if (typeName.equals("boolean") || typeName.equals("java.lang.Boolean")) {
                return Boolean.valueOf(parameter);
            }

            if (typeName.equals("java.util.Properties")) {
                Properties p = stringToProperties(parameter);
                if (p!= null) return p;
            }

            return parameter;
        } catch (NumberFormatException nfe) {
            _logger.log(Level.SEVERE, "jdbc.exc_nfe", parameter);
            String msg = sm.getString("me.invalid_param", parameter);
            throw new ResourceException(msg);
        }
    }

    public Object invokeMethod(Object object, String methodName,
            Class<?>[] valueTypes, Object... values) throws ResourceException {
        Object returnValue = null;
        Method actualMethod ;
        try {
            actualMethod = object.getClass().getMethod(methodName, valueTypes);
        } catch (NoSuchMethodException ex) {
            throw new ResourceException(ex);
        } catch (SecurityException ex) {
            throw new ResourceException(ex);
        }
        if (actualMethod != null) {
            try {
                returnValue = AccessController.doPrivileged(
                    (PrivilegedExceptionAction<Object>) () -> {
                            actualMethod.setAccessible(true);
                            return actualMethod.invoke(object, values);
                    });
            } catch (PrivilegedActionException e) {
                if(e.getException() != null){
                    throw new ResourceException(e.getException());
                }else{
                    throw new ResourceException(e);
                }
            }
        }
        return returnValue;
    }

    private Properties stringToProperties(String parameter)
    {
         if (parameter == null) return null;
         String s = parameter.trim();
         if (!((s.startsWith("(") && s.endsWith(")")))) {
            return null; // not a "( .... )" syntax
         }
         s = s.substring(1,s.length()-1);
         s = s.replaceAll("(?<!\\\\),",newline); // , -> \n
         s = s.replaceAll("\\\\,",",");  // escape-"," -> ,

         Properties p = new Properties();
         Properties prop = new Properties();
         try {
            p.load(new java.io.StringBufferInputStream(s));
         } catch (java.io.IOException ex) {
            if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST, "Parsing string to properties: {0}", ex.getMessage());
            }
            return null;
         }
         // cleanup trailing whitespace in value
         for (java.util.Enumeration propKeys = p.propertyNames();
               propKeys.hasMoreElements();) {
             String tmpKey = (String)propKeys.nextElement();
             String tmpValue = p.getProperty(tmpKey);
             // Trim spaces
             tmpValue = tmpValue.trim();
             // Quoted string.
             if (tmpValue.length() > 1 && tmpValue.startsWith("\"")
                     && tmpValue.endsWith("\"")) {
                tmpValue = tmpValue.substring(1,tmpValue.length()-1);
             }
             prop.put(tmpKey, tmpValue);
         }
         if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST, "Parsing string to properties: {0}size:{1}", new Object[]{prop, prop.size()});
         }
         return prop;
    }
}
