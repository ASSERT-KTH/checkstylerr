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

package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import java.util.Date;
import jakarta.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

//@jakarta.ejb.Stateful
public class StatefulWrapperImpl implements StatefulWrapper {

    private SessionContext context;
    private TimerStuff foo = null;

    /**
    private TopicConnection topicCon;
    private TopicSession topicSession;
    private TopicPublisher topicPublisher;
    private TopicSubscriber topicSubscriber;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    */

    public boolean doMessageDrivenTest(String jndiName,
                                    boolean jms) {
        boolean result = false;
        /**
        if( jms ) { return; }

        try {
System.out.println("********PG-> in doMessageDrivenTest() for jndiName = " + jndiName );

            setup(setup);
System.out.println("********PG-> in doMessageDrivenTest() after setup");
            Context ic = new InitialContext();
             Queue messageDrivenDest = (Queue) ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing message driven tests for" + jndiName);

            String testName;
            int numTests = 6;
            Timer ths[] = new Timer[numTests];
            for(int i = 1; i < numTests; i++) {
                testName = "test" + i;
                System.out.println("Doing " + testName);
                ths[i] = foo.createTimer(1000000, testName);
                ObjectMessage objMsg = queueSession.createObjectMessage(ths[i]);
                sendMsgs(messageDrivenDest, objMsg, 1);
            }

            long sleepTime = 30000;
            System.out.println("Sleeping for " + sleepTime / 1000 + " seconds");
            Thread.sleep(sleepTime);

            // at this point, all foo timers should have been cancelled
            // by the message bean.
            foo.assertNoTimers();
            result = true;

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
        **/

        return result;
    }

    private void setup() throws Exception {
        /**
//PG->        context = new InitialContext();

        TopicConnectionFactory topicConFactory =
            (TopicConnectionFactory) context.lookup
                ("java:comp/env/jms/MyTopicConnectionFactory");

System.out.println("********PG-> setup(): after  lookup");
        topicCon = topicConFactory.createTopicConnection();

System.out.println("********PG-> setup(): after  createTopicConnection");
        topicSession =
            topicCon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
System.out.println("********PG-> setup(): after  createTopicSession");

        // Producer will be specified when actual msg is published.
        topicPublisher = topicSession.createPublisher(null);
System.out.println("********PG-> setup(): after createPublisher");

        topicCon.start();
        System.out.println("********PG-> setup(): after start");

        QueueConnectionFactory queueConFactory =
            (QueueConnectionFactory) context.lookup
            ("java:comp/env/jms/MyQueueConnectionFactory");

        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE);

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);

        queueCon.start();
        **/

    }

    private void cleanup() {
        /**
        try {
            if( topicCon != null ) {
                topicCon.close();
            }
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
        **/
    }
    /**
    public void publishMsgs(Topic topic, Message msg, int num)
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Publishing message " + i + " to " + topic);
            topicPublisher.publish(topic, msg);
        }
    }

    public void sendMsgs(Queue queue, Message msg, int num)
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Publishing message " + i + " to " + queue);
            queueSender.send(queue, msg);
        }
    }
    */

    public boolean doFooTest(String jndiName, boolean jms) {
        boolean result = false;
        try {
            Context ic = new InitialContext();
            Object fooObjref = ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing foo timer test for " + jndiName);
            FooHome  fooHome = (FooHome)PortableRemoteObject.narrow
                (fooObjref, FooHome.class);

            foo = fooHome.create();
            if( jms ) {
                doJmsTest(foo);
            } else {
                doTest(foo);
            }

            result = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void doJmsTest(TimerStuff timerStuff) throws Exception {
        timerStuff.sendMessageAndCreateTimer();
        timerStuff.recvMessageAndCreateTimer(true);
        timerStuff.sendMessageAndCreateTimerAndRollback();
        timerStuff.recvMessageAndCreateTimerAndRollback(false);
    }

    private void doTest(TimerStuff timerStuff) throws Exception {

        System.out.println("doTest(): creating the runtimeExTimer ");
        Timer runtimeExTimer =
            timerStuff.createTimer(1, "RuntimeException");

        System.out.println("doTest(): creating the timer");
        Timer timer = timerStuff.createTimer(1, 1);

        //
        System.out.println("doTest(): creating the timer2");
        Timer timer2 = timerStuff.createTimer(10000, 10000);

        //
        System.out.println("doTest(): creating the timer3");
        Timer timer3 = timerStuff.createTimer(new Date());

        //
        System.out.println("doTest(): creating the timer4");
        Timer timer4 = timerStuff.createTimer(new Date(new Date().getTime() + 2000));

        //
        System.out.println("doTest(): creating the timer5");
        Timer timer5 = timerStuff.createTimer(new Date(new Date().getTime() + 20000), 10000);

        System.out.println("doTest(): creating the createTimerAndRollback");
        timerStuff.createTimerAndRollback(20000);

        //
        System.out.println("doTest(): creating the createTimerAndCancel");
        timerStuff.createTimerAndCancel(20000);

        // @@@ reevaluate double cancel logic
        //timerStuff.createTimerAndCancelAndCancel(20000);

        //
        System.out.println("doTest(): creating the createTimerAndCancelAndRollback");
        timerStuff.createTimerAndCancelAndRollback(20000);

        //
        System.out.println("doTest(): creating the cancelTimer(timer2)");
        timerStuff.cancelTimer(timer2);
        System.out.println("doTest(): assertTimerNotactive(timer2)");
        timerStuff.assertTimerNotActive(timer2);

        //
        timerStuff.cancelTimerAndRollback(timer5);
        // @@@ reevaluate double cancel logic
        //timerStuff.cancelTimerAndCancelAndRollback(timer6);

        Timer timer7 =
            timerStuff.createTimer(1, 1, "cancelTimer");
        Timer timer8 =
            timerStuff.createTimer(1, 1, "cancelTimerAndRollback");
        Timer timer9 =
            timerStuff.createTimer(1, "cancelTimerAndRollback");

        Timer timer11 = timerStuff.getTimeRemainingTest1(20);
        timerStuff.getTimeRemainingTest2(20, timer11);
        timerStuff.getTimeRemainingTest2(20, timer);

        Timer timer12 = timerStuff.getNextTimeoutTest1(20);
        timerStuff.getNextTimeoutTest2(20, timer12);
        timerStuff.getNextTimeoutTest2(20, timer);

        System.out.println("cancelling timer");
        timerStuff.cancelTimer(timer);

        System.out.println("cancelling timer5");
        timerStuff.cancelTimer(timer5);

        System.out.println("cancelling timer11");
        timerStuff.cancelTimer(timer11);

        System.out.println("cancelling timer12");
        timerStuff.cancelTimer(timer12);

        // It's possible that the following timers haven't expired yet
        try {
            timerStuff.cancelTimerNoError(timer8);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timer3);
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            timerStuff.cancelTimerNoError(timer4);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timer7);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(runtimeExTimer);
        } catch(Exception e) {
            e.printStackTrace();
        }

        timerStuff.assertNoTimers();
    }

    public void removeFoo() throws java.rmi.RemoteException, jakarta.ejb.RemoveException {
        if (foo != null) {
            ((Foo) foo).remove();
        }
    }
}
