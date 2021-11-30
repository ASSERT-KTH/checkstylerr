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

package com.sun.s1asdev.ejb.stubs.appclient.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.stubs.ejbapp.HelloHome;
import com.sun.s1asdev.ejb.stubs.ejbapp.Hello;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-stubs-appclient");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-stubs-appclientID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

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
            System.out.println("invoking ejb");
            hr.sayHello();

            System.out.println("successfully invoked ejb");
            stat.addStatus("appclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("appclient main" , stat.FAIL);
        }

            return;
    }

}

