/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.mdb.singleton.client;

import java.util.*;
import javax.naming.*;
import jakarta.jms.*;
import jakarta.annotation.*;
import jakarta.ejb.*;
import com.sun.s1asdev.ejb.mdb.singleton.FooRemoteIF;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * Tests for http://java.net/jira/browse/GLASSFISH-13004 (Support MDB singleton).
 * The test ejb jar is configured with property singleton-bean-pool=true in
 * sun-ejb-jar.xml.  This test client calls fooTest and doTest(...):
 * fooTest: verify the stateless bean FooBean is single instance;
 * doTest(..): verify the mdb MessageBean is single instance.
 */
public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-mdb-singleton");
        client.doTest();
        stat.printSummary("ejb-mdb-singletonID");
        System.exit(0);
    }

    private InitialContext context;
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private jakarta.jms.Queue clientQueue;

    private int numMessages = 1;
    private int numOfCalls = 120;

    @EJB
    private static FooRemoteIF foo;

    public Client(String[] args) {

        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void doTest() {
        try {
            setup();
            doTest("jms/ejb_mdb_singleton_InQueue", numMessages);
            fooTest();
            stat.addStatus("singleton main", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("singleton main", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void fooTest() {
        final Set<String> fooBeans = Collections.synchronizedSet(new HashSet<String>());
        Thread[] threads = new Thread[numOfCalls];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    fooBeans.add(foo.foo());
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        verifySingleInstance(fooBeans);
    }

    public void setup() throws Exception {
        context = new InitialContext();
        QueueConnectionFactory queueConFactory = (QueueConnectionFactory) context.lookup ("java:comp/env/FooCF");
        queueCon = queueConFactory.createQueueConnection();
        queueSession = queueCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queueSender = queueSession.createSender(null);
        queueCon.start();
    }

    public void cleanup() {
        try {
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void sendMsgs(jakarta.jms.Queue queue, Message msg, int num)
        throws JMSException {
        for(int i = 0; i < num; i++) {
            //System.out.println("Sending message " + i + " to " + queue +
            //                   " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
           // System.out.println("Sent message " + i + " to " + queue +
           //                    " at time " + System.currentTimeMillis());
        }
    }

    public void doTest(String destName, int num) throws Exception {
        Destination dest = (Destination) context.lookup(destName);

        for(int i = 0; i < numOfCalls; i++) {
            Message message = queueSession.createTextMessage(destName);
            //        Message message = topicSession.createTextMessage(destName);
            message.setBooleanProperty("flag", true);
            message.setIntProperty("num", i);
            sendMsgs((jakarta.jms.Queue) dest, message, num);
        }

        List<String> messageBeanInstances = new ArrayList<String>();
        int trials = 0;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            messageBeanInstances = foo.getMessageBeanInstances();
            ++trials;
        } while (messageBeanInstances.size() < numOfCalls && trials < 5);

        if(messageBeanInstances.size() <= 1 || messageBeanInstances.size() < numOfCalls) {
            throw new RuntimeException("Expecting number of instances " + numOfCalls + ", but got " +
                messageBeanInstances.size() + ": " + messageBeanInstances);
        }
        Set<String> messageBeanInstancesUnique = new HashSet<String>(messageBeanInstances);
        verifySingleInstance(messageBeanInstancesUnique);
    }

    private void verifySingleInstance(Collection<String> c) {
        if (c.size() == 1) {
            System.out.println("Got expected instances (single one): " + c);
        } else {
            throw new RuntimeException("Expecting single instance, but got " + c);
        }
    }
}

