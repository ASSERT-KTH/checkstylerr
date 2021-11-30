/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.jms.jmsejb.client;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import jakarta.jms.*;
import com.sun.s1asdev.ejb.jms.jmsejb.HelloHome;
import com.sun.s1asdev.ejb.jms.jmsejb.Hello;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-mdb-jmsejb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-mdb-jmsejbID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        String ejbNames[] = { "ejbs/hellobmt", "ejbs/hellocmt" };
        String ejbName = null;
        try {
            for(int i = 0; i < ejbNames.length; i++ ) {
                ejbName = ejbNames[i];

                Context ic = new InitialContext();

                System.out.println("Looking up ejb ref " + ejbName);
                // create EJB using factory from container
                // java.lang.Object objref = ic.lookup("helloApp/Hello");
                java.lang.Object objref = ic.lookup("java:comp/env/"+ejbName);
                System.out.println("---ejb stub---" + objref.getClass().getClassLoader());
                System.out.println("---ejb classname---" + objref.getClass().getName());
                System.out.println("---HelloHome---" + HelloHome.class.getClassLoader());
                System.err.println("Looked up home!!");

                HelloHome  home = (HelloHome)PortableRemoteObject.narrow(
                                                                         objref, HelloHome.class);
                System.err.println("Narrowed home!!");


                Hello hr = home.create(helloStr);
                System.err.println("Got the EJB!!");

                // invoke method on the EJB
                System.out.println("Asking ejb to send a message");
                String msgText = "Look ma, I received a JMS message!!!";

                //
                String result = hr.sendMessage1(msgText);
                System.out.println("Result from sendMessage = " + result);
                System.out.println("Asking ejb to receive a message");
                hr.receiveMessage1();

                //
                result = hr.sendMessage2(msgText);
                System.out.println("Result from sendMessage = " + result);
                System.out.println("Asking ejb to receive a message");
                hr.receiveMessage2();

                //
                result = hr.sendMessage3(msgText);
                System.out.println("Result from sendMessage = " + result);
                System.out.println("Asking ejb to receive a message");
                hr.receiveMessage3();

                /*  comment out
                hr.sendMessage4Part1(msgText);
                hr.sendMessage4Part2(msgText);

                hr.receiveMessage4Part1();
                hr.receiveMessage4Part2();
                */

                hr.sendAndReceiveMessage();

                hr.sendAndReceiveRollback();

                // Must be called in order
                hr.sendMessageRollback(msgText + "rollback");
                hr.receiveMessageRollback();

                stat.addStatus("jmsejb " + ejbName,
                               stat.PASS);
            }
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("jmsejb " + ejbName, stat.FAIL);
        }

            return;
    }

    final static String helloStr = "Hello World!";
}

