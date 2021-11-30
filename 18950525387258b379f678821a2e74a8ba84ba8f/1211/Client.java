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

package com.sun.s1asdev.ejb.ejb30.hello.session5.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session5.Sful2;
import com.sun.s1asdev.ejb.ejb30.hello.session5.Sless2;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session5");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session5ID");
    }

    /**
     * This test ensures that we don't assume a remote ejb client has
     * access to all the classes for all the remote business interfaces that
     * an invoked bean has.  SfulEJB and SlessEJB each have two distinct
     * remote business interfaces, but this client only uses Sful2 and
     * Sless2 and does not package the Sful or Sless classes at all. The
     * internal generated RMI-IIOP stubs created at runtime should not
     * cause any classloading errors in the client.
     */
    public Client (String[] args) {
    }

    @EJB(name="ejb/sful2", mappedName="shouldbeoverriddeninsunejbjar.xml")
    private static Sful2 sful2;

    @EJB(name="ejb/sless2", mappedName="ejb_ejb30_hello_session5_Sless")
    private static Sless2 sless2;

    public void doTest() {

        try {

            System.out.println("invoking stateful2");
            sful2.hello2();

            System.out.println("invoking stateless2");
            sless2.hello2();
            sless2.foo(1);

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

}

