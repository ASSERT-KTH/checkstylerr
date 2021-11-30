/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.appclient;

import com.sun.enterprise.security.ee.J2EESecurityManager;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.UsernamePasswordStore;
import com.sun.enterprise.security.appclient.integration.AppClientSecurityInfo;
import com.sun.enterprise.security.auth.login.LoginCallbackHandler;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.common.SecurityConstants;
import com.sun.enterprise.security.common.Util;
import com.sun.enterprise.security.jmac.config.GFAuthConfigFactory;
import com.sun.enterprise.security.integration.AppClientSSL;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.logging.LogDomains;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.config.AuthConfigFactory;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Security;
import org.glassfish.appclient.client.acc.config.Ssl;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.enterprise.iiop.api.IIOPSSLUtil;

/**
 *
 * @author Kumar
 */
@Service
public class AppClientSecurityInfoImpl implements AppClientSecurityInfo {

    private static Logger _logger=null;
    static {
        _logger=LogDomains.getLogger(AppClientSecurityInfoImpl.class, LogDomains.SECURITY_LOGGER);
    }

    private static final String DEFAULT_PARSER_CLASS = "com.sun.enterprise.security.appclient.ConfigXMLParser";

    private CallbackHandler callbackHandler;
    private CredentialType  appclientCredentialType;
    boolean isJWS;
    boolean useGUIAuth;
    private List<TargetServer> targetServers;
    private List<MessageSecurityConfig> msgSecConfigs;

    @Inject
    protected SSLUtils sslUtils;

    @Inject
    private SecurityServicesUtil secServUtil;
    @Inject
    private Util util;
    @Inject
    private IIOPSSLUtil appClientSSLUtil;

    @Override
    public void initializeSecurity(
            List<TargetServer> tServers,
            List<MessageSecurityConfig> configs, CallbackHandler handler,
            CredentialType credType, String username,
            char[] password, boolean isJWS, boolean useGUIAuth) {

           /* security init */
        this.isJWS = isJWS;
        this.useGUIAuth = useGUIAuth;
        this.appclientCredentialType = credType;
        if (handler != null) {
            this.callbackHandler = handler;
        } else {
            this.callbackHandler = new LoginCallbackHandler(useGUIAuth);
        }
        this.targetServers = tServers;
        this.msgSecConfigs = configs;

        SecurityManager secMgr = System.getSecurityManager();
        if (!isJWS && secMgr != null && !J2EESecurityManager.class.equals(secMgr.getClass())) {
            J2EESecurityManager mgr = new J2EESecurityManager();
            System.setSecurityManager(mgr);
        }
        if (_logger.isLoggable(Level.FINE)) {
            if (secMgr != null) {
                _logger.fine("acc.secmgron");
            } else {
                _logger.fine("acc.secmgroff");
            }
        }

        //set the parser to ConfigXMLParser
        System.setProperty("config.parser", DEFAULT_PARSER_CLASS);
        util.setAppClientMsgSecConfigs(msgSecConfigs);
        try {
            /* setup jsr 196 factory
             * define default factory if it is not already defined
             */
            String defaultFactory = java.security.Security.getProperty
            (AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY);
                _logger.fine("AuthConfigFactory obtained from java.security.Security.getProperty(\"authconfigprovider.factory\") :"
                        + ((defaultFactory != null) ? defaultFactory : "NULL"));
            if (defaultFactory == null) {
                java.security.Security.setProperty
                    (AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY,
                     GFAuthConfigFactory.class.getName());
            }

        } catch (Exception e) {
            _logger.log(Level.WARNING, "main.jmac_default_factory");
        }

        //TODO:V3 LoginContextDriver has a static variable dependency on AuditManager
        //And since LoginContextDriver has too many static methods that use AuditManager
        //we have to make this workaround here.
        //Handles in LoginContextDriver
        //LoginContextDriver.AUDIT_MANAGER = secServUtil.getAuditManager();

        //secServUtil.initSecureSeed();

        setSSLData(this.getTargetServers());
        if (username != null || password != null) {
            UsernamePasswordStore.set(username, password);
        }

        //why am i setting both?.
        secServUtil.setCallbackHandler(callbackHandler);
        util.setCallbackHandler(callbackHandler);
    }

    @Override
    public int getCredentialEncoding(CredentialType type) {
        switch(type) {
            case USERNAME_PASSWORD :
                return SecurityConstants.USERNAME_PASSWORD;
            case CERTIFICATE :
                return SecurityConstants.CERTIFICATE;
            case ALL :
                return SecurityConstants.ALL;
            default :
                throw new RuntimeException("Unknown CredentialType");
        }

    }

    @Override
    public Subject doClientLogin(CredentialType credType) {
        return LoginContextDriver.doClientLogin(this.getCredentialEncoding(credType), callbackHandler);
    }

    private AppClientSSL convert(Ssl ssl) {
        AppClientSSL appSSL = new AppClientSSL();
        appSSL.setCertNickname(ssl.getCertNickname());
        //appSSL.setClientAuthEnabled(ssl.isClientAuthEnabled());
        appSSL.setSsl2Ciphers(ssl.getSsl2Ciphers());
        appSSL.setSsl2Enabled(ssl.isSsl2Enabled());
        appSSL.setSsl3Enabled(ssl.isSsl3Enabled());
        appSSL.setSsl3TlsCiphers(ssl.getSsl3TlsCiphers());
        appSSL.setTlsEnabled(ssl.isTlsEnabled());
        appSSL.setTlsRollbackEnabled(ssl.isTlsRollbackEnabled());

        return appSSL;
    }

    private void setSSLData(List<TargetServer> tServers) {
        try {
            // Set the SSL related properties for ORB
            TargetServer tServer = tServers.get(0);
            // TargetServer is required.
            //temp solution to target-server+ change in DTD
            // assuming that multiple servers can be specified but only 1st
            // first one will be used.
            Security security = tServer.getSecurity();
            if (security == null) {
                _logger.fine("No Security input set in ClientContainer.xml");
                // do nothing
                return;
            }
            Ssl ssl = security.getSsl();
            if (ssl == null) {
                _logger.fine("No SSL input set in ClientContainer.xml");
                // do nothing
                return;

            }
            //XXX do not use NSS in this release
            //CertDb   certDB  = security.getCertDb();
            sslUtils.setAppclientSsl(convert(ssl));
            this.appClientSSLUtil.setAppClientSSL(convert(ssl));
        } catch (Exception ex) {

        }
    }

    public List<TargetServer> getTargetServers() {
        return targetServers;
    }

    public List<MessageSecurityConfig> getMsgSecConfigs() {
        return msgSecConfigs;
    }

    @Override
    public void clearClientSecurityContext() {
        ClientSecurityContext.setCurrent(null);
    }

    @Override
    public boolean isLoginCancelled() {
        boolean isCancelled = false;
        if(callbackHandler instanceof LoginCallbackHandler){
            isCancelled=((LoginCallbackHandler) callbackHandler).getCancelStatus();
        }
        return isCancelled;
    }
}
