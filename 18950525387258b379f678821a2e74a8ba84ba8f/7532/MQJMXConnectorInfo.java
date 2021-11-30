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

package org.glassfish.jms.admin.cli;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

/**
 * The <code>MQJMXConnectorInfo</code> holds MBean Server connection information
 * to a SJSMQ broker instance. This API is used by the admin infrastructure for
 * performing MQ administration/configuration operations on a broker instance.
 *
 * @author Sivakumar Thyagarajan
 * @since SJSAS 9.0
 */
public class MQJMXConnectorInfo {
    private String jmxServiceURL = null;
    private Map<String,?> jmxConnectorEnv = null;
    private String asInstanceName = null;
    private String brokerInstanceName = null;
    private String brokerType = null;
    private static final Logger _logger = Logger.getLogger(LogUtils.JMS_ADMIN_LOGGER);
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MQJMXConnectorInfo.class);
    private JMXConnector connector = null;

    public MQJMXConnectorInfo(String asInstanceName, String brokerInstanceName,
                              String brokerType, String jmxServiceURL,
                                       Map<String, ?> jmxConnectorEnv) {
        this.brokerInstanceName = brokerInstanceName;
        this.asInstanceName = asInstanceName;
        this.jmxServiceURL = jmxServiceURL;
        this.brokerType = brokerType;
        this.jmxConnectorEnv = jmxConnectorEnv;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "MQJMXConnectorInfo : brokerInstanceName " +
                            brokerInstanceName + " ASInstanceName " + asInstanceName +
                            " jmxServiceURL "  + jmxServiceURL +  " BrokerType " + brokerType
                            + " jmxConnectorEnv " + jmxConnectorEnv);
        }
    }

    public String getBrokerInstanceName(){
        return this.brokerInstanceName;
    }

    public String getBrokerType(){
        return this.brokerType;
    }

    public String getASInstanceName(){
        return this.asInstanceName;
    }

    public String getJMXServiceURL(){
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"MQJMXConnectorInfo :: JMXServiceURL is " + this.jmxServiceURL);
        }
        return this.jmxServiceURL;
    }

    public Map<String, ?> getJMXConnectorEnv(){
        return this.jmxConnectorEnv;
    }

    /**
     * Returns an <code>MBeanServerConnection</code> representing the MQ broker instance's MBean
     * server.
     * @return
     * @throws ConnectorRuntimeException
     */
    //XXX:Enhance to support SSL (once MQ team delivers support in the next drop)
    //XXX: Discuss how <code>ConnectionNotificationListeners</code> could
    //be shared with the consumer of this API
    public MBeanServerConnection getMQMBeanServerConnection() throws ConnectorRuntimeException {
        try {
            if (getJMXServiceURL() == null || getJMXServiceURL().equals("")) {
                String msg = localStrings.getLocalString("error.get.jmsserviceurl",
                                "Failed to get MQ JMXServiceURL of {0}.", getASInstanceName());
                throw new ConnectorRuntimeException(msg);
            }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                "creating MBeanServerConnection to MQ JMXServer with "+getJMXServiceURL());
            }
            JMXServiceURL jmxServiceURL = new JMXServiceURL(getJMXServiceURL());
            connector = JMXConnectorFactory.connect(jmxServiceURL, this.jmxConnectorEnv);
            //XXX: Do we need to pass in a Subject?
            MBeanServerConnection mbsc = connector.getMBeanServerConnection();
            return mbsc;
        } catch (Exception e) {
            e.printStackTrace();
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        }
    }

    public void closeMQMBeanServerConnection() throws ConnectorRuntimeException {
        try {
           if (connector != null) {
                 connector.close();
           }
        } catch (IOException e) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        }
    }
 }
