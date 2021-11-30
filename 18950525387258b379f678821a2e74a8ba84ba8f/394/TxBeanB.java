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

package com.sun.s1peqe.transaction.txstressreadonly.ejb.beanB;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

import java.sql.Statement;
import java.sql.ResultSet;
import jakarta.transaction.UserTransaction;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.RemoteException;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnectionFactory;


public class TxBeanB implements SessionBean {

    private Queue queue = null;
    private String user1 = null;
    private String user2 = null;
    private String dbURL1 = null;
    private String dbURL2 = null;
    private String password1 = null;
    private String password2 = null;
private UserTransaction tx = null;
    private SessionContext ctx = null;
    private QueueConnectionFactory qfactory = null;

    // ------------------------------------------------------------------------
    // Container Required Methods
    // ------------------------------------------------------------------------
    public void setSessionContext(SessionContext sc) {
        System.out.println("setSessionContext in BeanB");
        try {
            ctx = sc;
            Context ic = new InitialContext();
            user1 = (String) ic.lookup("java:comp/env/user1");
            password1 = (String) ic.lookup("java:comp/env/password1");
            user2 = (String) ic.lookup("java:comp/env/user2");
            password2 = (String) ic.lookup("java:comp/env/password2");
            dbURL1 = (String) ic.lookup("java:comp/env/dbURL1");
            dbURL2 = (String) ic.lookup("java:comp/env/dbURL2");
        } catch (Exception ex) {
            System.out.println("Exception in setSessionContext: " +
                               ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void ejbCreate() {
        System.out.println("ejbCreate in BeanB");
        try {
            Context context = new InitialContext();

            qfactory = (QueueConnectionFactory)
            context.lookup("java:comp/env/jms/QCFactory");

            queue = (Queue) context.lookup("java:comp/env/jms/SampleQueue");

            System.out.println("QueueConnectionFactory & " +
                               "Queue Initialized Successfully");
        } catch (Exception ex) {
            System.out.println("Exception in ejbCreate: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void ejbDestroy() {
        System.out.println("ejbDestroy in BeanB");
    }

    public void ejbActivate() {
        System.out.println("ejbActivate in BeanB");
    }

    public void ejbPassivate() {
        System.out.println("ejbPassivate in BeanB");
    }

    public void ejbRemove() {
        System.out.println("ejbRemove in BeanB");
    }

    // ------------------------------------------------------------------------
    // Business Logic Methods
    // ------------------------------------------------------------------------
    public void insert(String acc, float bal, int identity) throws RemoteException {
        tx = ctx.getUserTransaction();
        Connection con1 = null;
        Connection con2 = null;
        System.out.println("insert in BeanB");
        try {
        tx.begin();
        con1 = getConnection(dbURL1,user1,password1);
        Statement stmt1 = con1.createStatement();
        ResultSet rs = stmt1.executeQuery("SELECT * FROM txAccount");
        System.out.println("Account Read Successfully from DB1...");
        con2 = getConnection(dbURL2,user2,password2);
        Statement stmt2 = con2.createStatement();
        stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + (acc+identity) +
                             "', " + bal + ")");
        System.out.println("Account added Successfully in DB2...");
        stmt1.close();
            con1.close();
        stmt2.close();
        con2.close();
        tx.commit();
        tx.begin();
        con2 = getConnection(dbURL2,user2,password2);
        stmt2 = con2.createStatement();
        stmt2.executeUpdate("DELETE FROM txAccount WHERE account = '"
                            + (acc+identity) + "'");
        System.out.println("Account deleted Successfully in DB2...");
        stmt2.close();
        con2.close();
        tx.commit();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
            //    con1.close();
             //   con2.close();
            } catch (Exception ex) {
            }
        }
    }



    private Connection getConnection(String dbURL,String user, String password) {
        Connection con = null;
        System.out.println("getConnection in BeanB");
        try{
            Context context = new InitialContext();
            DataSource ds = (DataSource) context.lookup(dbURL);
            con = ds.getConnection(user, password);
            System.out.println("Got DB Connection Successfully...");
        } catch (Exception ex) {
            System.out.println("Exception in getConnection: " + ex.toString());
            ex.printStackTrace();
        }
        return con;
    }
}
