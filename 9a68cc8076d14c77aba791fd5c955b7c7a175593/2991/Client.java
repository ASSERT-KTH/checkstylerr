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

import javax.naming.InitialContext;

import java.util.concurrent.*;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    private RemoteAsync remoteAsync;

    private RemoteAsync2 statefulBean;
    private RemoteAsync3 statefulBeanLegacyRemote;
    private RemoteAsync3 statefulBeanLegacyRemote2;

    private int num;

    public static void main(String args[]) {

        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest();
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {
        num = Integer.valueOf(args[1]);
    }

    public void doTest() {

        try {

            statefulBean = (RemoteAsync2) new InitialContext().lookup("java:global/" + appName + "/StatefulBean!com.acme.RemoteAsync2");
            statefulBeanLegacyRemote = (RemoteAsync3) new InitialContext().lookup("java:global/" + appName + "/StatefulBean!com.acme.RemoteAsync3");
            statefulBeanLegacyRemote2 = (RemoteAsync3) new InitialContext().lookup("java:global/" + appName + "/StatefulBean!com.acme.RemoteAsync3");

            Future<String> futureSful = statefulBean.helloAsync();
            System.out.println("Stateful bean says " + futureSful.get());

            futureSful = statefulBean.removeAfterCalling();
            System.out.println("Stateful bean removed status = " + futureSful.get());

            boolean gotSfulException = false;
            try {
                futureSful = statefulBean.helloAsync();
            } catch(NoSuchEJBException nsee) {
                System.out.println("Got nsee from helloAsync");
                gotSfulException = true;
            }

            try {
                if( !gotSfulException ) {
                    System.out.println("return value = " + futureSful.get());
                    throw new EJBException("Should have gotten exception");
                }
            } catch(ExecutionException ee) {
                if( ee.getCause() instanceof NoSuchEJBException ) {
                    System.out.println("Successfully caught NoSuchEJBException when " +
                                       "accessing sful bean asynchronously after removal");
                } else {
                    throw new EJBException("wrong exception during sfsb access after removal",
                                           ee);
                }
            }

            try {
                Future<String> f = statefulBeanLegacyRemote.throwException("jakarta.ejb.CreateException");
                String result = f.get();
                throw new EJBException("Didn't get CreateException");
            } catch(ExecutionException ee) {
                if( ee.getCause() instanceof CreateException ) {
                    System.out.println("Successfully received CreateException");
                } else {
                    throw new EJBException("wrong exception received",
                                           ee);
                }
            }

            try {
                Future<String> f = statefulBeanLegacyRemote.throwException("jakarta.ejb.EJBException");
                String result = f.get();
                throw new EJBException("Didn't get EJBException");
            } catch(ExecutionException ee) {
                if( ee.getCause() instanceof RemoteException ) {
                    System.out.println("Successfully received RemoteException");
                } else {
                    throw new EJBException("wrong exception received",
                                           ee);
                }
            }


            try {
                Future<String> f = statefulBeanLegacyRemote2.removeAfterCalling();
                String result = f.get();
                System.out.println("result of removeAfterCalling = " + result);
            } catch(ExecutionException ee) {
                throw new EJBException("got unexpected exception", ee);
            }

            try {
                Future<String> f = statefulBeanLegacyRemote2.helloAsync();
                String result = f.get();
                throw new EJBException("Didn't get RemoteException");
            } catch(ExecutionException ee) {
                if( ee.getCause() instanceof NoSuchObjectException ) {
                    System.out.println("Successfully received RemoteException");
                } else {
                    throw new EJBException("wrong exception received",
                                           ee);
                }
            } catch(NoSuchObjectException nsoe) {
                System.out.println("Successfully received NoSuchObjectException");
            }

            remoteAsync = (RemoteAsync) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");
            remoteAsync.startTest();

            ExecutorService executor = Executors.newCachedThreadPool();

            int numFireAndForgets = num;
            for(int i = 0; i < numFireAndForgets; i++) {
                executor.execute( new FireAndForget() );
            }

            CancelAfterAlreadyDone cad = new CancelAfterAlreadyDone();
            executor.execute( cad );

            ProcessAsync pAsyncs[] = new ProcessAsync[num];
            for(int i = 0; i < pAsyncs.length; i++) {

                if( ( i % 2 ) == 0 ) {
                    pAsyncs[i] = new ProcessAsync(i, 1, 3, false);
                } else {
                    pAsyncs[i] = new ProcessAsync(i, 1, 3, true);
                }

                executor.execute( pAsyncs[i] );
            }

            executor.shutdown();

            executor.awaitTermination(15, TimeUnit.SECONDS);

            if( !cad.success ) {
                throw new Exception("Cancel after already done failed");
            }

            for(int i = 0; i < pAsyncs.length; i++) {
                ProcessAsync pa = pAsyncs[i];
                if( !pa.success() ) {
                    throw new Exception(pa.failureMsg);
                }
            }

            int ffCount =  remoteAsync.getFireAndForgetCount();
            System.out.println("FireAndForget count = " + ffCount + " expected = " +
                               numFireAndForgets);
            if( ffCount != numFireAndForgets) {
                throw new Exception("numFireAndForget mismatch");
            }

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }
    }

    class FireAndForget implements Runnable {
        public void run() {
            remoteAsync.fireAndForget();
        }
    }

    class CancelAfterAlreadyDone implements Runnable {
        boolean success = false;
        public void run() {
            try {
                // asyc method that returns immediately.
                // Sleep for a bit so that it's likely already
                // done by the time we call cancel.  This should
                // exercise the path that the result piggy-backs
                // on the return of the cancel call.
                Future<String> future = remoteAsync.helloAsync();
                Thread.sleep(2000);
                future.cancel(true);
                String result = future.get();
                System.out.println("cancel after done = " + result);
                success = true;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    class ProcessAsync implements Runnable {
        int id;
        int interval;
        int numIntervals;
        boolean cancel;

        boolean cancelled;
        boolean completed;

        String failureMsg;
        Throwable exception;

        public ProcessAsync(int i, int interval, int numIntervals, boolean cancel) {
            this.id = i;
            this.interval = interval;
            this.numIntervals = numIntervals;
            this.cancel = cancel;
        }
        public void run() {
            try {
                Future<Integer> future =
                    remoteAsync.processAsync(interval, numIntervals);
                if( cancel ) {
                    future.cancel(true);
                }

                Integer result = future.get();

                System.out.println("ProcessAsync result : " + result);
                completed = true;

            } catch(ExecutionException ee) {
                exception = ee.getCause();
                if( exception.getClass().getName().equals("java.lang.Exception") ) {
                    System.out.println("ProcessAsync succesfully cancelled");
                    cancelled = true;
                }
            } catch(Exception e) {
                exception = e;
            }
        }

        public boolean success() {
            boolean succeeded = true;
            if (cancel && !cancelled) {
                succeeded = false;
                failureMsg = "pasync " + id + " was not cancelled successfully";
            } else if( !cancel && !completed ) {
                succeeded = false;
                failureMsg = "pasync " + id + " did not complete successfully";
            }

            return succeeded;
        }

    }


}
