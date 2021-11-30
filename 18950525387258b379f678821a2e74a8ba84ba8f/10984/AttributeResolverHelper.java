/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.config;

import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.jvnet.hk2.config.TranslationException;
import org.jvnet.hk2.config.VariableResolver;

/**
 * Helper to resolve attribute configuration values eg
 * ${com.sun.aas.installRoot} once they have already been obtained in "raw"
 * form. If the goal is to fetch the attribute values in already-resolved form,
 * do so directly via
 *
 * @{link AttributeResolver#resolveAttribute}. <p> Values can be resolved into
 * String, boolean or int. <p> Example usage:</b>
 * <pre>
 * HTTPListenerConfig l = ...; // or any AMXConfigProxy sub-interface
 * AttributeResolverHelper h = new AttributeResolverHelper( l );
 * int port = h.resolveInt( l.getPort() );
 * </pre> Alternately, the static method form can be used:<br>
 * <pre>
 * HTTPListenerConfig l = ...; // or any AMXConfigProxy sub-interface
 * int port = AttributeResolverHelper.resolveInt( l, value );
 * </pre>
 */
@Taxonomy( stability = Stability.UNCOMMITTED)
public class AttributeResolverHelper extends VariableResolver {

    private static void debug(final String s) {
        System.out.println("##### " + s);
    }

    public AttributeResolverHelper(final AMXConfigProxy amx) {
    }

    @Override
    protected String getVariableValue(final String varName) throws TranslationException {
        String result = varName;

        // first look for a system property
        final Object value = System.getProperty(varName);
        if (value != null) {
            result = "" + value;
        }
        // Removed code that walked hierarchy since this is not called.
        return result;
    }

    /**
     * Return true if the string is a template string of the for ${...}
     */
    public static boolean needsResolving(final String value) {
        return value != null && value.indexOf("${") >= 0;
    }

    /**
     * Resolve the String using the target resolver (MBean).
     */
    public String resolve(final String in) throws TranslationException {
        final String result = translate(in);

        //debug( "AttributeResolverHelper.resolve(): " + in + " ===> " + result );

        return result;
    }
}
