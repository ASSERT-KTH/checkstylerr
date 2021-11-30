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

package com.sun.s1peqe.transaction.txlao.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txlao.ejb.beanA.*;

import java.rmi.RemoteException;


public class Client {

    private TxRemoteHomeA home = null;
    private SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public Client() {
    }

    public static void main(String[] args) {
        System.out.println("\nStarting Txglobal Test Suite");
        Client client = new Client();

        // initialize the context and home object
        client.setup();

        // run the tests
        client.runTestClient();
    }

    public void setup() {
        Class homeClass = TxRemoteHomeA.class;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanA");
            home = (TxRemoteHomeA) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
        } catch (Throwable ex) {
            System.out.println("Exception in setup: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void runTestClient() {
        try{
            status.addDescription("This is to test the global transaction!");
            firstXAJDBCSecondNonXAJDBC();
            firstNonXAJDBCSecondXAJDBC();
            firstXAJDBCSecondXAJDBC();
            firstXAJMSSecondNonXAJDBC() ;
            firstNonXAJDBCOnly()  ;
            rollbackXAJDBCNonXAJDBC();
            rollbackNonXAJDBCXAJDBC();
            cleanup();
            status.printSummary("txglobalID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstXAJDBCSecondNonXAJDBC() {
       try {
            System.out.println("Execute BeanA::firstXAJDBCSecondNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstXAJDBCSecondNonXAJDBC();

            if (result) {
                status.addStatus("txlao firstXAJDBCSecondNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstXAJDBCSecondNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstXAJDBCSecondNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstXAJDBCSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstNonXAJDBCSecondXAJDBC(){
      try {
            System.out.println("Execute BeanA::firstNonXAJDBCSecondXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstNonXAJDBCSecondXAJDBC();

            if (result) {
                status.addStatus("txlao firstNonXAJDBCSecondXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstNonXAJDBCSecondXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstNonXAJDBCSecondXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstNonXAJDBCSecondXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstXAJDBCSecondXAJDBC() {
       try {
            System.out.println("Execute BeanA::firstXAJDBCSecondXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstXAJDBCSecondXAJDBC();

            if (result) {
                status.addStatus("txlao firstXAJDBCSecondXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstXAJDBCSecondXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstXAJDBCSecondXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstXAJDBCSecondXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstNonXAJDBCSecondNonXAJDBC() {
        try {
            System.out.println("Execute BeanA::firstNonXAJDBCSecondNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstNonXAJDBCSecondNonXAJDBC();

            if (result) {
                status.addStatus("txlao firstNonXAJDBCSecondNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstNonXAJDBCSecondNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstNonXAJDBCSecondNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstNonXAJDBCSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstXAJMSSecondNonXAJDBC() {
        try {
            System.out.println("Execute BeanA::firstXAJMSSecondNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstXAJMSSecondNonXAJDBC();

            if (result) {
                status.addStatus("txlao firstXAJMSSecondNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstXAJMSSecondNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstXAJMSSecondNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstXAJMSSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
     public void firstNonXAJDBCOnly() {
      try {
            System.out.println("Execute BeanA::firstNonXAJDBCOnly");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstNonXAJDBCOnly();

            if (result) {
                status.addStatus("txlao firstNonXAJDBCOnly: ", status.PASS);
            } else {
                status.addStatus("txlao firstNonXAJDBCOnly: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstNonXAJDBCOnly: ", status.FAIL);
            System.out.println("Exception in firstNonXAJDBCOnly: " + ex.toString());
            ex.printStackTrace();
        }
    }

     public void rollbackXAJDBCNonXAJDBC() {
        try {
            System.out.println("Execute BeanA::rollbackXAJDBCNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.rollbackXAJDBCNonXAJDBC();

            if (result) {
                status.addStatus("txlao rollbackXAJDBCNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao rollbackXAJDBCNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao rollbackXAJDBCNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in rollbackXAJDBCNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
     }

     public void rollbackNonXAJDBCXAJDBC() {
     try {
            System.out.println("Execute BeanA::rollbackNonXAJDBCXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.rollbackNonXAJDBCXAJDBC();

            if (result) {
                status.addStatus("txlao rollbackNonXAJDBCXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao rollbackNonXAJDBCXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao rollbackNonXAJDBCXAJDBC: ", status.FAIL);
            System.out.println("Exception in rollbackNonXAJDBCXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
     }
     public void cleanup() {
      try {
            System.out.println("Execute BeanA::cleanup");

            TxRemoteA beanA = home.create();
            beanA.cleanup();

            beanA.remove();
        } catch (Exception ex) {
            System.out.println("Exception in cleanup: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void testTxCommit() {
        try {
            System.out.println("Execute BeanA::testTxCommit");

            TxRemoteA beanA = home.create();
            boolean result = beanA.txCommit();

            if (result) {
                status.addStatus("txlao testTxCommit: ", status.PASS);
            } else {
                status.addStatus("txlao testTxCommit: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao testTxCommit: ", status.FAIL);
            System.out.println("Exception in testTxCommit: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void testTxRollback() {
        try {
            System.out.println("Execute BeanA::testTxRollback");

            TxRemoteA beanA = home.create();
            boolean result = beanA.txRollback();

            if (result) {
                status.addStatus("txlao testTxRollback: ", status.PASS);
            } else {
                status.addStatus("txlao testTxRollback: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao testTxRollback: ", status.FAIL);
            System.out.println("Exception in testTxRollback: " + ex.toString());
            ex.printStackTrace();
        }
    }
}
