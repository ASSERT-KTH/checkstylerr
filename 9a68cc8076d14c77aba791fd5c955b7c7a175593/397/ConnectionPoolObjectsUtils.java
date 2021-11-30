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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.spi.TransactionSupport;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.config.types.Property;

import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is an util class for creating poolObjects of the type
 * ConnectorConnectionPool from ConnectorDescriptor and also using the
 * default values.
 *
 * @author Srikanth P
 */

public final class ConnectionPoolObjectsUtils {
    private ConnectionPoolObjectsUtils() { /* disallow instantiation */ }

    public static final String ELEMENT_PROPERTY = "ElementProperty";
    private static final Logger _logger = LogDomains.getLogger(ConnectionPoolObjectsUtils.class,LogDomains.RSR_LOGGER);

    private static final String VALIDATE_ATMOST_EVERY_IDLE_SECS =
            "com.sun.enterprise.connectors.ValidateAtmostEveryIdleSecs";

    private static final String validateAtmostEveryIdleSecsProperty = System.getProperty(VALIDATE_ATMOST_EVERY_IDLE_SECS);

    private static final StringManager localStrings =
            StringManager.getManager(ConnectionPoolObjectsUtils.class);

    /**
     * Creates default ConnectorConnectionPool consisting of default
     * pool values.
     *
     * @param poolInfo Name of the pool
     * @return ConnectorConnectionPool created ConnectorConnectionPool instance
     */

    public static ConnectorConnectionPool createDefaultConnectorPoolObject(
            PoolInfo poolInfo, String rarName) {

        ConnectorConnectionPool connectorPoolObj =
                new ConnectorConnectionPool(poolInfo);
        connectorPoolObj.setMaxPoolSize("20");
        connectorPoolObj.setSteadyPoolSize("10");
        connectorPoolObj.setMaxWaitTimeInMillis("7889");
        connectorPoolObj.setIdleTimeoutInSeconds("789");
        connectorPoolObj.setPoolResizeQuantity("2");
        connectorPoolObj.setFailAllConnections(false);
        connectorPoolObj.setMatchConnections(true); //always


        setDefaultAdvancedPoolAttributes(connectorPoolObj);

        try {
            connectorPoolObj.setTransactionSupport(getTransactionSupportFromRaXml(rarName));
        } catch (Exception ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("error in setting txSupport");
            }
        }
        return connectorPoolObj;
    }

    /**
     * Sets default values for advanced pool properties<br>
     *
     * @param connectorPoolObj Connector Connection Pool
     */
    private static void setDefaultAdvancedPoolAttributes(ConnectorConnectionPool connectorPoolObj) {
        //Other advanced attributes like connection-leak-reclaim, lazy-connection-enlistment,
        //lazy-connection-association, associate-with-thread are boolean values which are not required
        //to be explicitly initialized to default values.
        connectorPoolObj.setMaxConnectionUsage(ConnectorConnectionPool.DEFAULT_MAX_CONNECTION_USAGE);
        connectorPoolObj.setConnectionLeakTracingTimeout(ConnectorConnectionPool.DEFAULT_LEAK_TIMEOUT);
        connectorPoolObj.setConCreationRetryAttempts(ConnectorConnectionPool.DEFAULT_CON_CREATION_RETRY_ATTEMPTS);
        connectorPoolObj.setConCreationRetryInterval(ConnectorConnectionPool.DEFAULT_CON_CREATION_RETRY_INTERVAL);
        connectorPoolObj.setValidateAtmostOncePeriod(ConnectorConnectionPool.DEFAULT_VALIDATE_ATMOST_ONCE_PERIOD);
    }

    /**
     * Creates ConnectorConnectionPool object pertaining to the pool props
     * mentioned in the sun-ra/xml i.e it represents the pool mentioned in the
     * sun-ra.xm.
     *
     * @param poolInfo Name of the pool
     * @param desc     ConnectorDescriptor which represent ra.xml and sun-ra.xml.
     * @return ConnectorConnectionPool created ConnectorConnectionPool instance
     */
    public static ConnectorConnectionPool createSunRaConnectorPoolObject(
            PoolInfo poolInfo, ConnectorDescriptor desc, String rarName) {

        ConnectorConnectionPool connectorPoolObj =
                new ConnectorConnectionPool(poolInfo);
        SunConnector sundesc = desc.getSunDescriptor();
        ResourceAdapter sunRAXML = sundesc.getResourceAdapter();

        connectorPoolObj.setMaxPoolSize(
                (String) sunRAXML.getValue(ResourceAdapter.MAX_POOL_SIZE));
        connectorPoolObj.setSteadyPoolSize(
                (String) sunRAXML.getValue(ResourceAdapter.STEADY_POOL_SIZE));
        connectorPoolObj.setMaxWaitTimeInMillis((String) sunRAXML.getValue(
                ResourceAdapter.MAX_WAIT_TIME_IN_MILLIS));
        connectorPoolObj.setIdleTimeoutInSeconds((String) sunRAXML.getValue(
                ResourceAdapter.IDLE_TIMEOUT_IN_SECONDS));
        connectorPoolObj.setPoolResizeQuantity((String) "2");
        connectorPoolObj.setFailAllConnections(false);
        connectorPoolObj.setMatchConnections(true); //always


        setDefaultAdvancedPoolAttributes(connectorPoolObj);


        try {
            connectorPoolObj.setTransactionSupport(getTransactionSupportFromRaXml(rarName));
        } catch (Exception ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("error in setting txSupport");
            }
        }

        boolean validateAtmostEveryIdleSecs = false;

        //For SunRAPool, get the value of system property VALIDATE_ATMOST_EVERY_IDLE_SECS.
        if (validateAtmostEveryIdleSecsProperty != null && validateAtmostEveryIdleSecsProperty.equalsIgnoreCase("TRUE")) {
            validateAtmostEveryIdleSecs = true;
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "CCP.ValidateAtmostEveryIdleSecs.Set", poolInfo);
            }
        }
        connectorPoolObj.setValidateAtmostEveryIdleSecs(validateAtmostEveryIdleSecs);

        return connectorPoolObj;
    }

    /**
     * Return the interger representation container transaction support value equivalent to
     * the jakarta.resource.spi.TransactionSupport enum value.
     *
     * @param mcfTS jakarta.resource.spi.TransactionSupport
     * @return container equivalent value
     */
    public static int convertSpecTxSupportToContainerTxSupport(
            jakarta.resource.spi.TransactionSupport.TransactionSupportLevel mcfTS) {
        int containerEquivalentValue ;
        switch (mcfTS) {
            case LocalTransaction:
                containerEquivalentValue =  ConnectorConstants.LOCAL_TRANSACTION_INT;
                break;
            case NoTransaction:
                containerEquivalentValue = ConnectorConstants.NO_TRANSACTION_INT;
                break;
            case XATransaction:
                containerEquivalentValue = ConnectorConstants.XA_TRANSACTION_INT;
                break;
            default :
                containerEquivalentValue = ConnectorConstants.UNDEFINED_TRANSACTION_INT;
                break;
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("convertSpecTxSupportToContainerTxSupport: passed in mcfTransactionSupport =>" + mcfTS + ", " +
                    "converted container equivalent value: " + containerEquivalentValue);
        }
        return containerEquivalentValue;
    }


    /**
     * Return the integer representation of the transaction-support attribure
     *
     * @param txSupport one of <br>
     *                  <ul>
     *                  <li>NoTransaction</li>
     *                  <li>LocalTransaction</li>
     *                  <li>XATransaction</li>
     *                  </ul>
     * @return one of
     *         <ul>
     *         <li>ConnectorConstants.UNDEFINED_TRANSACTION_INT</li>
     *         <li>ConnectorConstants.NO_TRANSACTION_INT</li>
     *         <li>ConnectorConstants.LOCAL_TRANSACTION_INT</li>
     *         <li>ConnectorConstants.XA_TRANSACTION_INT</li>
     *         </ul>
     */
    public static int parseTransactionSupportString(String txSupport) {
        int txSupportIntVal = ConnectorConstants.UNDEFINED_TRANSACTION_INT;


        if (txSupport == null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("txSupport is null");
            }
            return txSupportIntVal;
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("parseTransactionSupportString: passed in txSupport =>" + txSupport);
        }

        if (ConnectorConstants.NO_TRANSACTION_TX_SUPPORT_STRING.equals(txSupport)) {
            txSupportIntVal = ConnectorConstants.NO_TRANSACTION_INT;
        } else if (ConnectorConstants.LOCAL_TRANSACTION_TX_SUPPORT_STRING.equals(txSupport)) {
            txSupportIntVal = ConnectorConstants.LOCAL_TRANSACTION_INT;
        } else if (ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING.equals(txSupport)) {
            txSupportIntVal = ConnectorConstants.XA_TRANSACTION_INT;
        }

        return txSupportIntVal;
    }

    public static boolean isTxSupportConfigurationSane(int txSupport, String raName) {
        int raXmlTxSupport = ConnectorConstants.UNDEFINED_TRANSACTION_INT;

        try {
            raXmlTxSupport = ConnectionPoolObjectsUtils.getTransactionSupportFromRaXml(raName);
        } catch (Exception e) {
            _logger.log(Level.WARNING,
                    (e.getMessage() != null ? e.getMessage() : "  "));
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "isTxSupportConfigSane:: txSupport => "
                    + txSupport + "  raXmlTxSupport => " + raXmlTxSupport);
        }

        return (txSupport <= raXmlTxSupport);
    }


    /**
     * A utility method to map TransactionSupport ints as represented
     * in ConnectorConstants to the new TransactionSupport enum
     */
    public static TransactionSupport getTransactionSupport(int ts) {
        switch (ts) {
            case ConnectorConstants.NO_TRANSACTION_INT:
                return TransactionSupport.NO_TRANSACTION;
            case ConnectorConstants.LOCAL_TRANSACTION_INT:
                return TransactionSupport.LOCAL_TRANSACTION;
            case ConnectorConstants.XA_TRANSACTION_INT:
                return TransactionSupport.XA_TRANSACTION;
        }
        return null;
    }

    public static String getValueFromMCF(String prop, PoolInfo poolInfo,
                                         ManagedConnectionFactory mcf) {
        String result = null;
        try {
            Method m = mcf.getClass().getMethod("get" + prop, (java.lang.Class[]) null);
            result = (String) m.invoke(mcf, (java.lang.Object[]) null);
        } catch (Throwable t) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, t.getMessage(), t);
            }
        }

        return result == null ? "" : result;
    }

    public static Subject createSubject(ManagedConnectionFactory mcf,
                                        final ResourcePrincipal prin) {

        final Subject tempSubject = new Subject();
        if (prin != null) {
            String password = prin.getPassword();
            if (password != null) {
                final PasswordCredential pc =
                        new PasswordCredential(prin.getName(),
                                password.toCharArray());
                pc.setManagedConnectionFactory(mcf);
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        tempSubject.getPrincipals().add(prin);
                        tempSubject.getPrivateCredentials().add(pc);
                        return null;
                    }
                });
            }
        }
        return tempSubject;
    }

    public static boolean isPoolSystemPool(org.glassfish.connectors.config.ConnectorConnectionPool
            domainCcp) {
        String poolName = domainCcp.getName();
        return isPoolSystemPool(poolName);
    }

    public static boolean isPoolSystemPool(String poolName) {
        Pattern pattern = Pattern.compile("#");
        Matcher matcher = pattern.matcher(poolName);

        // If the pool name does not contain #, return false
        if (!matcher.find()) {
            return false;
        }

        matcher.reset();

        String moduleNameFromPoolName = null;
        int matchCount = 0;

        while (matcher.find()) {
            matchCount++;
            int patternStart = matcher.start();
            moduleNameFromPoolName = poolName.substring(0, patternStart);
        }

        // If pool name contains more than 2 #, return false as the
        // default system pool will have exacly one # for a standalone rar
        // and exactly two #s for an embedded rar
        ResourcesUtil resUtil = ResourcesUtil.createInstance();
        switch (matchCount) {

            case 1:
                if (resUtil.belongToStandAloneRar(moduleNameFromPoolName))
                    return true;
            default:
                return false;
        }
    }


     /**
     * Validates and sets the values for LazyConnectionEnlistment and LazyConnectionAssociation.
     * @param lazyAssocString Property value
     * @param adminPool  Config Bean
     * @param conConnPool Connector Connection Pool
     */
    public static void setLazyEnlistAndLazyAssocProperties(String lazyAssocString, List<Property> properties,
                                                           ConnectorConnectionPool conConnPool){

        //Get LazyEnlistment value.
        //To set LazyAssoc to true, LazyEnlist also need to be true.
        //If LazyAssoc is true and LazyEnlist is not set, set it to true.
        //If LazyAssoc is true and LazyEnlist is false, throw exception.


        if (properties == null) return ;

        Property lazyEnlistElement = null;

         for(Property property : properties){
             if(property.getName().equalsIgnoreCase("LAZYCONNECTIONENLISTMENT")){
                 lazyEnlistElement = property;
             }
         }

            boolean lazyAssoc = toBoolean( lazyAssocString, false );
            if(lazyEnlistElement != null){

                boolean lazyEnlist = toBoolean(lazyEnlistElement.getValue(),false);
                if(lazyAssoc){
                    if(lazyEnlist){
                        conConnPool.setLazyConnectionAssoc( true) ;
                        conConnPool.setLazyConnectionEnlist( true);
                    }else{
                         _logger.log(Level.SEVERE,"conn_pool_obj_utils.lazy_enlist-lazy_assoc-invalid-combination",conConnPool.getName());
                        String i18nMsg = localStrings.getString(
               "cpou.lazy_enlist-lazy_assoc-invalid-combination");
                        throw new RuntimeException(i18nMsg + conConnPool.getName());
                    }
                }else{
                    conConnPool.setLazyConnectionAssoc(false);
                }
            }else{
                if(lazyAssoc){
                    conConnPool.setLazyConnectionAssoc( true) ;
                    conConnPool.setLazyConnectionEnlist( true);
                }else{
                    conConnPool.setLazyConnectionAssoc( false) ;
                }
            }
}

    private static boolean toBoolean( Object prop, boolean defaultVal ) {
        if ( prop == null ) {
            return defaultVal;
        }
        return Boolean.valueOf(((String) prop).toLowerCase(Locale.getDefault()));
     }

    public static int getTransactionSupportFromRaXml(String rarName) throws
            ConnectorRuntimeException {
        String txSupport =
                ConnectorRuntime.getRuntime().getConnectorDescriptor(rarName).
                        getOutboundResourceAdapter().getTransSupport();

        return parseTransactionSupportString(txSupport);
    }
}
