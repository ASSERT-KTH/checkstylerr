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

import jakarta.jms.*;
import jakarta.ejb.*;

public class MessageDrivenEJB extends TimerStuffImpl
    implements MessageDrivenBean, TimedObject, MessageListener {
    private MessageDrivenContext mdc;

    public MessageDrivenEJB(){
    }

    public void onMessage(Message message) {
        try {
            ObjectMessage objMsg = (ObjectMessage) message;
            Timer t = (Timer) objMsg.getObject();
            String info = (String) t.getInfo();

            boolean redelivered = message.getJMSRedelivered();
            System.out.println("Received message " + info + " , redelivered = " + redelivered);

            if (info.equals("test1") ) {
                System.out.println("In onMessage : Got t for timer = " +
                                   t.getInfo());
                doTimerStuff("onMessage", true);
                getInfo(t);
                getNextTimeoutTest2(5, t);
                getTimeRemainingTest2(5, t);

                cancelTimer(t);

                createTimerAndCancel(10000000);

                Timer t1 = createTimer(1000000, "messagedrivenejb");
                cancelTimer(t1);
                Timer t2 = createTimer(10000, "messagedrivenejb");
            } else if( info.equals("test2") ) {
                if( redelivered ) {
                    cancelTimer(t);
                } else {
                    if( isBMT() ) {
                        cancelTimer(t);
                    } else {
                        cancelTimerAndRollback(t);
                    }
                }
            } else if( info.equals("test3") ) {
                if( redelivered ) {
                    getInfo(t);
                    cancelTimer(t);
                } else {
                    cancelTimer(t);
                    createTimerAndRollback(10000000);
                }
            } else if( info.equals("test4") ) {
                cancelTimer(t);
                Timer runtimeExTimer =
                    createTimer(1, "RuntimeException");
            } else if( info.equals("test5") ) {
                cancelTimer(t);
                createTimer(1, 1, "cancelTimer");
            } else if( info.equals("test6") ) {
                cancelTimer(t);
                Timer ctar = createTimer(1, 1, "cancelTimerAndRollback");
                cancelTimer(ctar);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void ejbTimeout(Timer t) {
        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In MessageDrivenEJB::ejbTimeout --> "
                               + t.getInfo());
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
        }

        try {
            handleEjbTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
        }

    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        this.mdc = mdc;
        setContext(mdc);
        System.out.println("In ejbnonpersistenttimer.MessageDrivenEJB::setMessageDrivenContext !!");
        checkCallerSecurityAccess("setMessageDrivenContext", false);

        getTimerService("setMessageDrivenContext", false);
        doTimerStuff("setMessageDrivenContext", false);
    }

    public void ejbCreate() throws EJBException {
        System.out.println("In ejbnonpersistenttimer.MessageDrivenEJB::ejbCreate !!");
        setupJmsConnection();
        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);
        getTimerService("ejbCreate", true);
        doTimerStuff("ejbCreate", false);
    }

    public void ejbRemove() {
        System.out.println("In ejbnonpersistenttimer.MessageDrivenEJB::ejbRemove !!");
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", true);
        doTimerStuff("ejbRemove", false);
        cleanup();
    }

}
