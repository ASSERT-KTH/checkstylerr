/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ejb.*;
import jakarta.annotation.*;
import jakarta.annotation.security.*;
import org.omg.CORBA.ORB;
import java.util.concurrent.*;

@Singleton
    @Remote({ Hello.class, Hello2.class})
//    @Remote(Hello.class)
@LocalBean
    @ConcurrencyManagement(ConcurrencyManagementType.BEAN)
    @EJB(name="java:app/env/forappclient", beanInterface=Hello.class)
public class SingletonBean {

    @Resource
    private ORB orb;

    @EJB
    private SingletonBean me;

    @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("orb = " + orb);
        if( orb == null ) {
            throw new EJBException("null ORB");
        }

        Hello meViaAppClientDefinedDependency = (Hello)
            sessionCtx.lookup("java:app/env/appclientdefinedejbref1");
        System.out.println("meViaAppClientDefinedDependency =" +
                           meViaAppClientDefinedDependency);
        Hello meViaAppClientDefinedDependency2 = (Hello)
            sessionCtx.lookup("java:app/appclientdefinedejbref2");

        Hello meViaAppClientDefinedDependency3 = (Hello)
            sessionCtx.lookup("java:global/appclientdefinedejbref3");

        String appLevelEnvEntry = (String)
            sessionCtx.lookup("java:app/env/enventry1");
        System.out.println("appLevelEnvEntry = " + appLevelEnvEntry);

        String globalLevelEnvEntry = (String)
            sessionCtx.lookup("java:global/enventry2");
        System.out.println("globalLevelEnvEntry = " + globalLevelEnvEntry);

    }

    public void blah() { }

    @RolesAllowed("foo")
    public void protectedSyncRemote() {
        System.out.println("In SingletonBean::protectedSyncRemote cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
    }
    @PermitAll
    public void unprotectedSyncRemote() {
        System.out.println("In SingletonBean::unprotectedSyncRemote cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
    }

    @RolesAllowed("foo")
    @Asynchronous
    public Future<Object> protectedAsyncRemote() {
        System.out.println("In SingletonBean::protectedAsyncRemote cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
        return new AsyncResult<Object>(new String());
    }

    @Asynchronous
    @PermitAll
    public Future<Object> unprotectedAsyncRemote() {
        System.out.println("In SingletonBean::unprotectedAsyncRemote cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
        return new AsyncResult<Object>(new String());
    }

    @RolesAllowed("foo")
    @Asynchronous
    public Future<Object> protectedAsyncLocal() {
        System.out.println("In SingletonBean::protectedAsyncLocal cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
        return new AsyncResult<Object>(new String());
    }

    @Asynchronous
    @PermitAll
    public Future<Object> unprotectedAsyncLocal() {
        System.out.println("In SingletonBean::unprotectedAsyncLocal cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
        return new AsyncResult<Object>(new String());
    }

    @RolesAllowed("foo")
    public void protectedSyncLocal() {
        System.out.println("In SingletonBean::protectedSyncLocal cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
        return;
    }


    @PermitAll
    public void unprotectedSyncLocal() {
        System.out.println("In SingletonBean::unprotectedSyncLocal cp = " +
                           sessionCtx.getCallerPrincipal()  + " , " +
                           Thread.currentThread());
        return ;
    }

    @PermitAll
    public void testProtectedSyncLocal() {
        me.protectedSyncLocal();
    }

    @PermitAll
    public void testProtectedAsyncLocal() {
        try {
            Future<Object> future = me.protectedAsyncLocal();
            Object obj = future.get();
        } catch(Exception ee) {
            if( ee.getCause() instanceof EJBAccessException) {
                throw (EJBAccessException) ee.getCause();
            }
        }
    }

    @PermitAll
    public void testUnprotectedSyncLocal() {
        me.unprotectedSyncLocal();

    }

    @PermitAll
    public void testUnprotectedAsyncLocal() {
        try {
            Future<Object> future = me.unprotectedAsyncLocal();
            Object obj = future.get();
            // Success
        } catch(Exception ee) {
            throw (EJBException) new EJBException("Got unexpected exception").initCause(ee.getCause());
        }
    }



    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
