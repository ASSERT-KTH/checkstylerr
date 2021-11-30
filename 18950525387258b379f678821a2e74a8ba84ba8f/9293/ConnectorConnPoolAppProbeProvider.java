/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.monitor;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Probe provider interface for Connector connection pool related events
 * to provide information related to the various objects on
 * connector connection pool monitoring grouped by applications.
 *
 * @author Shalini M
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="connector-pool", probeProviderName="applications")
public class ConnectorConnPoolAppProbeProvider extends ConnectionPoolAppProbeProvider {
    /**
     * Emits probe event/notification that the given connector connection pool
     * <code>poolName</code> for the <code>appName</code> has got a
     * decrement connections used event.
     *
     * @param poolName for which decrement numConnUsed is got
     * @param appName for which decrement numConnUsed is got
     */
    @Probe(name="decrementConnectionUsedEvent")
    public void decrementConnectionUsedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName) { }

    /**
     * Emits probe event/notification that the given connector connection pool
     * <code>poolName</code> for the <code>appName</code> has got an
     * increment connections used event.
     *
     * @param poolName for which increment numConnUsed is got
     * @param appName for which increment numConnUsed is got
     */
    @Probe(name="connectionUsedEvent")
    public void connectionUsedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName) { }

    /**
     * Emits probe event/notification that a connection is acquired by application
     * for the given jca connection pool <code>poolName</code> by the
     * <code>appName</code>
     *
     * @param poolName
     * @param appName
     */
    @Probe(name="connectionAcquiredEvent")
    public void connectionAcquiredEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName) { }

    /**
     * Emits probe event/notification that a connection is released for the given
     * jca connection pool <code>poolName</code> by the
     * <code>appName</code>
     *
     * @param poolName
     * @param appName
     */
    @Probe(name="connectionReleasedEvent")
    public void connectionReleasedEvent(@ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName) { }

}
