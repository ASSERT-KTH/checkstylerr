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

package org.glassfish.flashlight.client;

import org.glassfish.external.probe.provider.annotations.ProbeParam;

import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */
public class EjbContainerListener {

    @ProbeListener("ejb:container::entry")
    public void foo(@ProbeParam("method")Method m, @ProbeParam("beanName")String beanName) {
        System.out.println("Got callback for: " + beanName);
    }

    @ProbeListener("ejb:container::entry")
    public void foo2(@ProbeParam("beanName")String beanName,
                     @ProbeParam("$appName")String applicationName,
                     @ProbeParam("method")Method m) {
        System.out.println("Got callback for: " + applicationName
                + "[" + beanName + "]");
    }

}
