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

package com.sun.s1asdev.ejb.txprop.simple;

import java.util.Date;
import java.util.Collection;
import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.TimerService;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import jakarta.transaction.UserTransaction;
import com.sun.s1asdev.ejb.txprop.simple.HelloHome;
import com.sun.s1asdev.ejb.txprop.simple.Hello;

public class FooBean implements SessionBean {

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() throws RemoteException {
        System.out.println("In FooBean::ejbCreate !!");

        // getting UserTransaction() is allowed
        System.out.println("Calling getUserTransaction()");
        UserTransaction ut = sc.getUserTransaction();

        try {
            // Calling ut methods is not allowed here.
            ut.getStatus();
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught illegal state ex when " +
                               "accessing UserTransaction methods in " +
                               "SLSB ejbCreate");
        } catch(Exception se) {
            throw new EJBException(se);
        }
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void callHello()  {
        System.out.println("in FooBean::callHello()");

        try {
            Context ic = new InitialContext();

            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");

            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);

            System.err.println("Narrowed home!!");

            Hello hr = home.create();
            System.err.println("Got the EJB!!");

            // invoke method on the EJB
            System.out.println("starting user tx");

            sc.getUserTransaction().begin();

            System.out.println("invoking ejb with user tx");

            hr.sayHello();

            System.out.println("successfully invoked ejb");

            System.out.println("committing tx");
            sc.getUserTransaction().commit();

        } catch(Exception e) {
            try {
                sc.getUserTransaction().rollback();
            } catch(Exception re) { re.printStackTrace(); }
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }

        TimerService timerService = sc.getTimerService();

        try {
            timerService.createTimer(new Date(), 1000, null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " +
                               ise.getMessage());
        }
        try {
            timerService.createTimer(new Date(), null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " +
                               ise.getMessage());
        }
        try {
            timerService.createTimer(1000, 1000, null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " +
                               ise.getMessage());
        }
        try {
            timerService.createTimer(1000, null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " +
                               ise.getMessage());
        }

        Collection timers = timerService.getTimers();
        if( timers.size() > 0 ) {
            throw new EJBException("Wrong number of timers : " +
                                   timers.size());
        } else {
            System.out.println("Successfully retrieved 0 timers");
        }

        Package p = this.getClass().getPackage();
        if( p == null ) {
            throw new EJBException("null package for " +
                                   this.getClass().getName());
        } else {
            System.out.println("Package name = " + p);
        }

    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
