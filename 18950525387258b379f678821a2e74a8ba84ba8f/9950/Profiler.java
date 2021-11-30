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

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import static org.glassfish.config.support.Constants.NAME_REGEX;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 *
 * Profilers could be one of jprobe, optimizeit, hprof, wily and so on jvm-options and property elements are used to
 * record the settings needed to get a particular profiler going. A server instance is tied to a particular profiler, by
 * the profiler element in java-config. Changing the profiler will require a server restart
 *
 * The adminstrative graphical interfaces, could list multiple supported profilers (incomplete at this point) and will
 * populate server.xml appropriately.
 *
 */

/* @XmlType(name = "", propOrder = {
    "jvmOptionsOrProperty"
}) */

@Configured
public interface Profiler extends ConfigBeanProxy, PropertyBag, JvmOptionBag {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = false) // bizarre case of having a name, but it's not a key; it's a singleton
    @NotNull
    @Pattern(regexp = NAME_REGEX)
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the classpath property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getClasspath();

    /**
     * Sets the value of the classpath property.
     *
     * @param value allowed object is {@link String }
     */
    public void setClasspath(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nativeLibraryPath property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getNativeLibraryPath();

    /**
     * Sets the value of the nativeLibraryPath property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNativeLibraryPath(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

}
