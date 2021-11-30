/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

import java.util.HashMap;
import java.util.Map;

/**
 * Inject configuration values to the object.
 *
 * The service name indicates the element name in XML config.
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
public abstract class ConfigInjector<T> {
    /**
     * Reads values from {@link Dom} and inject them into the given target object.
     *
     * @throws ConfigurationException
     *      If the injection fails. This exception won't have its location set yet.
     *      It's the caller's job to do so.
     */
    public abstract void inject(Dom dom, T target);

    /**
     * Injects a single property of the given element name.
     */
    public abstract void injectElement(Dom dom, String elementName, T target );

    /**
     * Injects a single property of the given attribute name.
     */
    public abstract void injectAttribute(Dom dom, String attributeName, T target );

    // utility methods for derived classes
    public final int asInt(String v) {
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(v+" is not a number");
        }
    }

    private static final Map<String,Boolean> BOOLEAN_VALUES = new HashMap<String,Boolean>();

    static {
        for( String t : new String[]{"on","true","1","yes","enabled"})
           BOOLEAN_VALUES.put(t,true);
        for( String t : new String[]{"off","false","0","no","disabled"})
           BOOLEAN_VALUES.put(t,false);
    }

    public final boolean asBoolean(String v) {
        Boolean b = BOOLEAN_VALUES.get(v);
        if(b!=null) return b;
        throw new ConfigurationException(v+" is not a boolean");
    }

    /**
     * Resolves a reference to the given type by the given id.
     */
    public final <T> T reference(Dom dom, String id, Class<T> type) {
        // TODO: this doesn't work in case where type is a subtype of indexed type.
        String name = type.getName();
        dom = dom.getSymbolSpaceRoot(name);
        return type.cast(dom.resolveReference(id,name).get());
    }
}
