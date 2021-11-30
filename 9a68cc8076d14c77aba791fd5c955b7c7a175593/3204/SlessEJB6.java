/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb31.aroundtimeout;


import jakarta.ejb.Stateless;
import jakarta.ejb.Schedule;
import jakarta.ejb.Timer;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.InvocationContext;


// Each interceptor list must at least have InterceptorG for
// aroundTimeoutCalled state to be set correctly.

@Stateless
@ExcludeDefaultInterceptors
public class SlessEJB6 implements Sless6
{
    boolean aroundTimeoutCalled = false;
    boolean aroundInvokeCalled = false;

    private final static int EXPECTED = 10;

    @EJB SlessEJB7 sless7;

    // Called as a timeout only. InterceptorG has 2 separate aroundXXX calls.
    @Interceptors({InterceptorA.class, InterceptorG.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-ag")
    public void ag() {
        System.out.println("in SlessEJB6:ag().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("InterceptorG aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6-ag");
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-ag")
    public void ag(Timer t) {
        System.out.println("in SlessEJB6:ag(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6_Timer-ag");
    }

    // bg() (but not bg(param)) marked through ejb-jar.xml
    // as having aroundtimeout B,G
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-bg")
    public void bg() {
        System.out.println("in SlessEJB6:bg().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6-bg");
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-bg")
    public void bg(Timer t) {
        System.out.println("in SlessEJB6:bg(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6_Timer-bg");
    }

    // overloaded version of interceptor-binding used in ejb-jar.xml to
    // mark all methods with name cg as having aroundtimeout C,G
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-cg")
    public void cg() {
        System.out.println("in SlessEJB6:cg().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6-cg");
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-cg")
    public void cg(Timer t) {
        System.out.println("in SlessEJB6:cg(Timer t).  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6_Timer-cg");
    }

    // Kind of like ag(), in that dg() is overloaded, but it's the
    // signature that has a parameter that is assigned aroundtimeout using
    // @Interceptor.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-dg")
    public void dg() {
        System.out.println("in SlessEJB6:dg().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6-dg");
    }

    @Interceptors({InterceptorD.class, InterceptorG.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-dg")
    public void dg(Timer t) {
        System.out.println("in SlessEJB6:dg(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6_Timer-dg");
    }



    // Like dg(), except that dg(Timer t) is assigned its interceptor
    // chain through ejb-jar.xml
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6-eg")
    public void eg() {
        System.out.println("in SlessEJB6:eg().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        Common.storeResult("SlessEJB6-eg");
    }

    // Called through interface only. InterceptorG has
    // 2 separate aroundXXX calls.
    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void noaroundtimeout() {
        System.out.println("in SlessEJB6:noaroundtimeout().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        if( aroundTimeoutCalled ) {
            aroundTimeoutCalled = false;
            throw new EJBException("bean class aroundTimeout was incorrectly called");
        }
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
        aroundInvokeCalled = false;
    }

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB6_Timer-eg")
    public void eg(Timer t) {
        System.out.println("in SlessEJB6:eg(Timer t).  " +
                           "aroundTimeoutCalled = " + aroundTimeoutCalled);

        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        aroundTimeoutCalled = false;
        Common.storeResult("SlessEJB6_Timer-eg");
    }

    public void verify() {
        Common.checkResults("SlessEJB6", EXPECTED);
            System.out.println("verifying Sless7 tests");

            sless7.verify();
            sless7.dc();

    }

}


