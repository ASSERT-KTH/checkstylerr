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

package com.acme;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class HelloSingleton implements Hello {

    @Resource
    SessionContext sessionCtx;

    @Resource(name = "java:app/env/myString")
    protected String myString;

    @EJB(name = "java:app/env/appLevelEjbRef")
    private Hello hello;

    String appName;
    String moduleName;

    @PostConstruct
    private void init() {
        System.out.println("HelloSingleton::init()");

        System.out.println("myString = '" + myString + "'");
        if ((myString == null) || !(myString.equals("myString"))) {
            throw new RuntimeException("Invalid value " + myString
                    + " for myString");
        }

        appName = (String) sessionCtx.lookup("java:app/AppName");
        moduleName = (String) sessionCtx.lookup("java:module/ModuleName");

        System.out.println("AppName = " + appName);
        System.out.println("ModuleName = " + moduleName);

    }

    public String hello() {

        System.out.println("HelloSingleton::hello()");

        return "hello, world!\n";
    }

    @PreDestroy
    private void destroy() {
        System.out.println("HelloSingleton::destroy()");
    }

}
