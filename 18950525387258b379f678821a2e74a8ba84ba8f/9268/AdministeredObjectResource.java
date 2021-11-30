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

package com.sun.enterprise.resource.beans;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.logging.LogDomains;
import org.glassfish.resources.api.JavaEEResource;
import org.glassfish.resources.api.JavaEEResourceBase;
import org.glassfish.resources.naming.SerializableObjectRefAddr;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import javax.naming.Reference;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resource infor for Connector administered objects
 *
 * @author Qingqing Ouyang
 */
public class AdministeredObjectResource extends JavaEEResourceBase {

    private final static Logger _logger = LogDomains.getLogger(AdministeredObjectResource.class, LogDomains.RSR_LOGGER);

    private String resadapter_;
    private String adminObjectClass_;
    private String adminObjectType_;
    private Set configProperties_;


    public AdministeredObjectResource(ResourceInfo resourceInfo) {
        super(resourceInfo);
    }

    protected JavaEEResource doClone(ResourceInfo resourceInfo) {
        AdministeredObjectResource clone =
                new AdministeredObjectResource(resourceInfo);
        clone.setResourceAdapter(getResourceAdapter());
        clone.setAdminObjectType(getAdminObjectType());
        return clone;
    }


    public int getType() {
        // FIX ME
        return 0;
        //return J2EEResource.ADMINISTERED_OBJECT;
    }

    public void initialize(AdminObject desc) {
        configProperties_ = new HashSet();
        adminObjectClass_ = desc.getAdminObjectClass();
        adminObjectType_ = desc.getAdminObjectInterface();
    }

    public String getResourceAdapter() {
        return resadapter_;
    }

    public void setResourceAdapter(String resadapter) {
        resadapter_ = resadapter;
    }

    public String getAdminObjectType() {
        return adminObjectType_;
    }

    public void setAdminObjectType(String adminObjectType) {
        this.adminObjectType_ = adminObjectType;
    }

    public void setAdminObjectClass(String name) {
        this.adminObjectClass_ = name;
    }

    public String getAdminObjectClass() {
        return this.adminObjectClass_;
    }

    /*
     * Add a configProperty to the set
     */
    public void addConfigProperty(ConnectorConfigProperty  configProperty) {
        this.configProperties_.add(configProperty);
    }

    /**
     * Add a configProperty to the set
     */
    public void removeConfigProperty(ConnectorConfigProperty  configProperty) {
        this.configProperties_.remove(configProperty);
    }

    public Reference createAdminObjectReference() {
        Reference ref =
                new Reference(getAdminObjectType(),
                        new SerializableObjectRefAddr("jndiName", this),
                        ConnectorConstants.ADMINISTERED_OBJECT_FACTORY, null);

        return ref;
    }


    // called by com.sun.enterprise.naming.factory.AdministeredObjectFactory
    // FIXME.  embedded??
    public Object createAdministeredObject(ClassLoader jcl)
            throws PoolingException {

        try {
            if (jcl == null) {
                // use context class loader
                jcl = (ClassLoader) AccessController.doPrivileged
                        (new PrivilegedAction() {
                            public Object run() {
                                return
                                        Thread.currentThread().getContextClassLoader();
                            }
                        });
            }


            Object adminObject =
                    jcl.loadClass(adminObjectClass_).newInstance();

            AccessController.doPrivileged
                    (new SetMethodAction(adminObject, configProperties_));

        // associate ResourceAdapter if the admin-object is RAA
        if(adminObject instanceof ResourceAdapterAssociation){
            try {
                ResourceAdapter ra = ConnectorRegistry.getInstance().
                        getActiveResourceAdapter(resadapter_).getResourceAdapter();
                ((ResourceAdapterAssociation) adminObject).setResourceAdapter(ra);
            } catch (ResourceException ex) {
                _logger.log(Level.SEVERE, "rardeployment.assoc_failed", ex);
            }
        }

            // At this stage, administered object is instantiated, config properties applied
            // validate administered object

            //ConnectorRuntime should be available in CLIENT mode now as admin-object-factory would have bootstapped
            //connector-runtime.
            ConnectorRuntime.getRuntime().getConnectorBeanValidator().validateJavaBean(adminObject, resadapter_);

            return adminObject;
        } catch (PrivilegedActionException ex) {
            throw(PoolingException) (new PoolingException().initCause(ex));
        } catch (Exception ex) {
            throw(PoolingException) (new PoolingException().initCause(ex));
        }
    }

    public String toString() {
        return "< Administered Object : " + getResourceInfo() +
                " , " + getResourceAdapter() +
                " , " + getAdminObjectType() + " >";
    }
}
