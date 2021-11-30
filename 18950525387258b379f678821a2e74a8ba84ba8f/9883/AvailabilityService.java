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

package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;

import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jvnet.hk2.config.DuckTyped;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "webContainerAvailability",
    "ejbContainerAvailability",
    "jmsAvailability",
    "property"
}) */

@Configured

public interface AvailabilityService extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the availabilityEnabled property.
     *
     * This boolean flag controls whether availability is enabled for HTTP session persistence. If this is "false", then
     * session persistence is disabled for all web modules in j2ee apps and stand-alone web modules. If it is "true" (and
     * providing that the global availability-enabled in availability-service is also "true", then j2ee apps and stand-alone
     * web modules may be ha enabled. Finer-grained control exists at lower levels. If this attribute is missing, it
     * "inherits" the value of the global availability-enabled under availability-service.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getAvailabilityEnabled();

    /**
     * Sets the value of the availabilityEnabled property.
     *
     * @param value allowed object is {@link String }
     */
    void setAvailabilityEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the haAgentHosts property.
     *
     * Comma-delimited list of server host names or IP addresses where high availability store management agents are
     * running.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Deprecated
    String getHaAgentHosts();

    /**
     * Sets the value of the haAgentHosts property.
     *
     * @param value allowed object is {@link String }
     */
    void setHaAgentHosts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the haAgentPort property.
     *
     * Port number where highly available store management agents can be contacted
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Max(value = 65535)
    @Min(value = 1)
    @Deprecated
    String getHaAgentPort();

    /**
     * Sets the value of the haAgentPort property.
     *
     * @param value allowed object is {@link String }
     */
    void setHaAgentPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the haAgentPassword property. password needed to contact highly available store management agents
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Deprecated
    String getHaAgentPassword();

    /**
     * Sets the value of the haAgentPassword property.
     *
     * @param value allowed object is {@link String }
     */
    void setHaAgentPassword(String value) throws PropertyVetoException;

    /**
     * Gets the value of the haStoreName property. Name of the session store
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Deprecated
    String getHaStoreName();

    /**
     * Sets the value of the haStoreName property.
     *
     * @param value allowed object is {@link String }
     */
    void setHaStoreName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the autoManageHaStore property. If set to true, the lifecycle of the highly available store is
     * matched with the lifecycle of the highly available cluster. The store is started or stopped with the cluster. It is
     * removed when the cluster is deleted. When set to false, the store lifecycle would have to manually managed by the
     * administrator.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    @Deprecated
    String getAutoManageHaStore();

    /**
     * Sets the value of the autoManageHaStore property.
     *
     * @param value allowed object is {@link String }
     */
    void setAutoManageHaStore(String value) throws PropertyVetoException;

    /**
     * Gets the value of the storePoolName property.
     *
     * This is the jndi-name for the JDBC Connection Pool used potentially by both the Web Container and the EJB Stateful
     * Session Bean Container for use in checkpointing/passivation when persistence-type = "ha". See
     * sfsb-ha-persistence-type and sfsb-persistence-type for more details. It will default to "jdbc/hastore". This
     * attribute can be over-ridden in either web-container-availability (with http-session-store-pool-name) and/or in
     * ejb-container-availability (with sfsb-store-pool-name). If store-pool-name is not over-ridden then both containers
     * will share the same connection pool. If either container over-rides then it may have its own dedicated pool. In this
     * case there must also be a new corresponding JDBC Resource and JDBC Connection Pool defined for this new pool name.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Deprecated
    String getStorePoolName();

    /**
     * Sets the value of the storePoolName property.
     *
     * @param value allowed object is {@link String }
     */
    void setStorePoolName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the haStoreHealthcheckEnabled property.
     *
     * Application server stops saving session state when the store service does not function properly or is is not
     * accessible for any reason. When this attribute is set to true, periodic checking is done to detect if the store
     * service has become available again. If healthcheck succeeds the session state saving is resumed.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    @Deprecated
    String getHaStoreHealthcheckEnabled();

    /**
     * Sets the value of the haStoreHealthcheckEnabled property.
     *
     * @param value allowed object is {@link String }
     */
    void setHaStoreHealthcheckEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the haStoreHealthcheckIntervalInSeconds property.
     *
     * The periodicity at which store health is checked.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "5")
    @Min(value = 1)
    @Deprecated
    String getHaStoreHealthcheckIntervalInSeconds();

    /**
     * Sets the value of the haStoreHealthcheckIntervalInSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    void setHaStoreHealthcheckIntervalInSeconds(String value) throws PropertyVetoException;

    @Element("*")
    List<AvailabilityServiceExtension> getExtensions();

    @DuckTyped
    <T extends AvailabilityServiceExtension> T getExtensionByType(Class<T> type);

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    @Override
    List<Property> getProperty();

    class Duck {
        public static <T extends AvailabilityServiceExtension> T getExtensionByType(AvailabilityService as, Class<T> type) {
            for (AvailabilityServiceExtension extension : as.getExtensions()) {
                try {
                    return type.cast(extension);
                } catch (Exception e) {
                    // ignore, not the right type.
                }
            }
            return null;
        }
    }
}
