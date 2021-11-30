/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.monitoring.probes;

import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.gmbal.Description;

/**
 * Probe emitter for the Ejb Pool monitoring events.
 * Used by the probe framework as an event notifier.
 *
 * @author Marina Vatkina
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="ejb", probeProviderName="cache")
public class EjbCacheProbeProvider {

    @Probe(name="beanPassivatedEvent")
    public void ejbBeanPassivatedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("success") boolean success) {}


    @Probe(name="expiredSessionsRemovedEvent")
    public void ejbExpiredSessionsRemovedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("num") long num) {}
}
