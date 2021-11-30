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

package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.glassfish.api.admin.config.ApplicationName;

import java.util.List;

/**
 * Element describing the system-applications in domain.xml
 *
 */

@Configured
public interface SystemApplications extends Applications {

    /**
     * Gets the value of the
     * lifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModuleorApplication
     * property. Objects of the following type(s) are allowed in the list {@link LifecycleModule } {@link J2eeApplication }
     * {@link EjbModule } {@link WebModule } {@link ConnectorModule } {@link AppclientModule } {@link Mbean }
     * {@link ExtensionModule } {@link Application }
     */
    @Element("*")
    public List<ApplicationName> getModules();

}
