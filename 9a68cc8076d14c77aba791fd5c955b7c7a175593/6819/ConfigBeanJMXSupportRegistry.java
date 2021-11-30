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

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;


/**
    A registry of ConfigBeanJMXSupport, for efficiency in execution time and scalability
    for large numbers of MBeans which share the same underlying type of @Configured.
 */
@Taxonomy( stability=Stability.NOT_AN_INTERFACE )
final class ConfigBeanJMXSupportRegistry
{
    private ConfigBeanJMXSupportRegistry() {}

    /**
        Map an interface to its helper.
     */
    private static final ConcurrentMap<Class<? extends ConfigBeanProxy>,ConfigBeanJMXSupport>  INSTANCES =
        new ConcurrentHashMap<Class<? extends ConfigBeanProxy>,ConfigBeanJMXSupport>();

    /**
     */
        public static ConfigBeanJMXSupport
    getInstance( final Class<? extends ConfigBeanProxy> intf )
    {
        if ( intf == null )
        {
            throw new IllegalArgumentException("null ConfigBeanProxy interface passed in" );
        }

        ConfigBeanJMXSupport helper = INSTANCES.get(intf);
        if ( helper == null )
        {
            // don't cache it, we can't be sure about its key
            helper = new ConfigBeanJMXSupport(intf, null);
        }
        return helper;
    }


    public static synchronized  List<Class<? extends ConfigBeanProxy>> getConfiguredClasses()
    {
        return new ArrayList<Class<? extends ConfigBeanProxy>>( INSTANCES.keySet() );
    }

        public static ConfigBeanJMXSupport
    getInstance( final ConfigBean configBean )
    {
        ConfigBeanJMXSupport helper = INSTANCES.get( configBean.getProxyType() );
        if ( helper == null )
        {
            helper = addInstance(configBean);
        }
        return helper;
    }

        private static synchronized ConfigBeanJMXSupport
    addInstance( final ConfigBean configBean )
    {
        final Class<? extends ConfigBeanProxy> intf = configBean.getProxyType();
        ConfigBeanJMXSupport helper = INSTANCES.get(intf);
        if ( helper == null )
        {
            helper = new ConfigBeanJMXSupport(configBean);
            INSTANCES.put( intf, helper );
        }
        return helper;
    }

    /** Find all  ConfigBeanProxy interfaces  reachable from specified item, including the item itself */
        public static Set<Class<? extends ConfigBeanProxy>>
    getAllConfigBeanProxyInterfaces( final ConfigBeanJMXSupport top) {
        final Set<Class<? extends ConfigBeanProxy>> all = new HashSet<Class<? extends ConfigBeanProxy>>();
        all.add( top.getIntf() );

        for( final Class<? extends ConfigBeanProxy>  intf : top.childTypes().values() )
        {
            all.addAll( getAllConfigBeanProxyInterfaces(getInstance(intf)) );
        }
        return all;
    }


    /** Recursively attempt to find default values for a descendant of specified type */
        public static Class<? extends ConfigBeanProxy>
    getConfigBeanProxyClassFor( final ConfigBeanJMXSupport start, final String type) {
        Class<? extends ConfigBeanProxy> result = start.childTypes().get(type);
        if ( result == null )
        {
            for( final String sub : start.childTypes().keySet() )
            {
                final Class<? extends ConfigBeanProxy> intf = start.childTypes().get(sub);
                final ConfigBeanJMXSupport spt = getInstance(intf);
                result = getConfigBeanProxyClassFor( spt, type );
                if ( result != null )
                {
                    break;
                }
            }
        }
        return result;
    }

 }



























