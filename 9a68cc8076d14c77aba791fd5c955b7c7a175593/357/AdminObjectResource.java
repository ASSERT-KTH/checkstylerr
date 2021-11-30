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

package org.glassfish.connectors.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import jakarta.validation.constraints.NotNull;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 *
 */
@Configured
@ResourceConfigCreator(commandName="create-admin-object")
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-admin-object"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-admin-object")
})
@ResourceTypeOrder(deploymentOrder=ResourceDeploymentOrder.ADMIN_OBJECT_RESOURCE)
@UniqueResourceNameConstraint(message="{resourcename.isnot.unique}", payload=AdminObjectResource.class)
/**
 * The admin-object-resource element describes a administered object
 * for a inbound resource adapter.
 */
public interface AdminObjectResource extends ConfigBeanProxy, BindableResource, Resource,
    PropertyBag {

    /**
     * Interface definition for the administered object
     * @return the interface definition
     */
    @Attribute
    @NotNull
    String getResType();

    /**
     * Sets the Interface definition for the administered object
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setResType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the resAdapter property.
     * Name of the inbound resource adapter
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    String getResAdapter();


    /**
     * Gets the value of the (admin object) classname property.
     * Name of the admin object class
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getClassName();

    /**
     * Sets the value of the (admin object) classname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setClassName(String value) throws PropertyVetoException;

    /**
     * Sets the value of the resAdapter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setResAdapter(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setDescription(String value) throws PropertyVetoException;

    /**
        Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(AdminObjectResource resource){
            return resource.getJndiName();
        }
    }

}
