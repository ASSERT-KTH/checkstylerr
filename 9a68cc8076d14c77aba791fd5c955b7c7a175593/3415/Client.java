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

package com.sun.s1asdev.ejb.stubs.proxy.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.stubs.proxy.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-stubs-proxy");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-stubs-proxyID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        try {

            Context ic = new InitialContext();

            // create EJB using factory from container
            java.lang.Object objref =
                ic.lookup("java:comp/env/ejb/ProxyApp");

            System.out.println("Looked up home!!");

            HelloHome  home = (HelloHome)
                PortableRemoteObject.narrow(objref, HelloHome.class);
            System.out.println("Narrowed home!!");

            Hello hr = home.create();
            System.out.println("Got the EJB!!");

            hr.testPassByRef();

            // invoke method on the EJB
            doProxyTest(hr);

            testExceptions(hr);

            hr.shutdown();

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

    private void testExceptions(Hello h) throws Exception {

        try {
            h.throwException();
        } catch(Exception e) {
            if( e.getClass() == Exception.class ) {
                System.out.println("Successfully caught exception " +
                                   e.getClass() + " " + e.getMessage());
            } else {
                throw e;
            }
        }

        try {
            h.throwAppException1();
            throw new Exception("didn't get exception for testException2");
        } catch(jakarta.ejb.FinderException e) {
            System.out.println("Successfully caught exception " +
                               e.getClass() + " " + e.getMessage());
        }

        try {
            h.throwAppException2();
            throw new Exception("didn't get exception for testException3");
        } catch(jakarta.ejb.FinderException e) {
            System.out.println("Successfully caught exception " +
                               e.getClass() + " " + e.getMessage());
        }

    }

    private void testNotImplemented(Common c) {
        try {
            c.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void testNotImplemented(CommonRemote cr) {
        try {
            cr.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void doProxyTest(Hello hr)
        throws Exception
    {
        System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
        hr.warmup(Common.STATEFUL);
        runTests(Common.STATEFUL, hr);

        System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");
        hr.warmup(Common.STATEFUL);
        runTests(Common.STATELESS, hr);

        System.out.println("\nBMP Entity results (microsec): \t\twith tx \tno tx:");
        hr.warmup(Common.BMP);
        runTests(Common.BMP, hr);
    }

    private void runTests(int type, Hello hr)
        throws Exception
    {

        hr.notSupported(type, true);
        hr.notSupported(type, false);
        hr.supports(type, true);
        hr.supports(type, false);
        hr.required(type, true);
        hr.required(type, false);
        hr.requiresNew(type, true);
        hr.requiresNew(type, false);
        hr.mandatory(type, true);
        hr.never(type, false);
    }
}

