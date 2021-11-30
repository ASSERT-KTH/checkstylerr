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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.sql.*;
import jakarta.ejb.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import jakarta.annotation.Resource;
import java.util.HashSet;

/**
 *
 * @author marina vatkina
 */

@Stateless
public class MyBean {

    private static final String DEF_RESOURCE = "jdbc/__default";
    private static final String XA_RESOURCE = "jdbc/xa";

    private HashSet<String> timers = new HashSet<String>();
    @Resource TimerService ts;

    @Timeout
    private void timeout(Timer t) {
        System.err.println("In ___MyBean:timeout___ "  + t.getInfo() );
        timers.add(""+t.getInfo());
    }


    public String verifydefault() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(DEF_RESOURCE);

        return ""+verify(ds);
   }

    public String verifyxa() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(XA_RESOURCE);

        return "" + verify(ds) + "+" + timers.size();
   }

    public int verify(DataSource ds) throws Exception {
        String selectStatement = "select * from student";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(selectStatement);
        ResultSet rs = ps.executeQuery();
        int result = 0;
        while (rs.next()) {
            result++;
            System.out.println("Found: " + rs.getString(1) + " : " + rs.getString(2));
        }
        rs.close();
        ps.close();
        c.close();

        return result;
    }

    public boolean create_timer(int id) throws Exception {
        ts.createTimer(1000, 5000, "timer01 " + id);
        return true;
    }

    public boolean testtwo(int id) throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds1 = (DataSource) initCtx.lookup(DEF_RESOURCE);
        DataSource ds2 = (DataSource) initCtx.lookup(XA_RESOURCE);

        boolean res1 = test(id, ds1, true);
        boolean res2 = test(id, ds2, true);
        return res1 && res2;
    }

    private boolean test(int id, DataSource ds, boolean useFailureInducer) throws Exception {
        String insertStatement = "insert into student values ( ? , ? )";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(insertStatement);

        if (useFailureInducer) {
            com.sun.jts.utils.RecoveryHooks.FailureInducer.activateFailureInducer();
            com.sun.jts.utils.RecoveryHooks.FailureInducer.setWaitPoint(com.sun.jts.utils.RecoveryHooks.FailureInducer.PREPARED, 60);
        }

        for (int i = 0; i < 3; i++) {
            System.err.println("Call # " + (i + 1));
            ps.setString(1, "BAA" + id + i);
            ps.setString(2, "BBB" + id + i);
            ps.executeUpdate();

            if (!useFailureInducer) {
                try {
                    Thread.sleep(7000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ps.close();
        c.close();
        System.err.println("Insert successfully");

        return true;
    }

}
