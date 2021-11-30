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

package org.glassfish.admin.amx.core;

import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

import javax.management.MBeanServerConnection;


/**
 * @deprecated Extra information available about each {@link AMXProxy}. Most
 *             of these fields are for advanced use and/or direct use of JMX.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface Extra extends StdAttributesAccess, MetaGetters, AMXProxy {

    /**
     * Get the MBeanServerConnection.
     */
    MBeanServerConnection mbeanServerConnection();


    /**
     * @return the {@link ProxyFactory} that created this proxy.
     */
    ProxyFactory proxyFactory();


    /** return a compilable Java interface representing this MBean */
    @Override
    String java();
}
