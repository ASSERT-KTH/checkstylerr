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

package org.glassfish.web.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Corresponds to the context-param element for customizing the configuration
 * of web apps.
 *
 * @author tjquinn
 */
@Configured
public interface ContextParam extends ConfigBeanProxy {

    /**
     * Returns the context-param description.
     * @return description
     */
    @Element
    public String getDescription();

    /**
     * Sets the context-param description.
     * @param value new description value
     */
    public void setDescription(String value);

    /**
     * Returns the context-param parameter name.
     * @return parameter name
     */
    @Element(required=true,key=true)
    public String getParamName();

    /**
     * Sets the context-param parameter name.
     * @param value
     */
    public void setParamName(String value);

    /**
     * Returns the context-param parameter value.
     * @return parameter value
     */
    @Element
    public String getParamValue();

    /**
     * Sets the context-param parameter value.
     * @param value new value
     */
    public void setParamValue(String value);

    @Attribute(dataType=Boolean.class, defaultValue="false")
    public String getIgnoreDescriptorItem();
    public void setIgnoreDescriptorItem(String value);

}
