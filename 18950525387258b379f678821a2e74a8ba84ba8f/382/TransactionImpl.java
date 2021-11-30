/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

import java.sql.SQLException;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.EISSystemException;

/**
 * This class captures the common methods between the spi and cci
 * LocalTransactionImpl classes.
 * @author Sheetal Vartak
 */
public class TransactionImpl {

  private CciManagedConnection mc;

  private boolean sendEvents;

  public TransactionImpl(CciManagedConnection mc, boolean sendEvents) {
    this.mc = mc;
    this.sendEvents = sendEvents;
  }

  public void begin() throws ResourceException {

    try {
      java.sql.Connection con = mc.getJdbcConnection();
      con.setAutoCommit(false);
      if (sendEvents) {
        mc.sendEvent(ConnectionEvent.LOCAL_TRANSACTION_STARTED, null);
      }
    }
    catch (SQLException ex) {
      ResourceException re = new EISSystemException(ex.getMessage());
      re.setLinkedException(ex);
      throw re;
    }
  }

  public void commit() throws ResourceException {
    java.sql.Connection con = null;
    try {
      con = mc.getJdbcConnection();
      con.commit();
      if (sendEvents) {
        mc.sendEvent(ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, null);
      }
    }
    catch (SQLException ex) {
      ResourceException re = new EISSystemException(ex.getMessage());
      re.setLinkedException(ex);
      throw re;
    }
    finally {
      try {
        con.setAutoCommit(true);
      }
      catch (Exception ex) {
      }
    }
  }

  public void rollback() throws ResourceException {
    java.sql.Connection con = null;
    try {
      con = mc.getJdbcConnection();
      con.rollback();
      if (sendEvents) {
        mc.sendEvent(ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, null);
      }
    }
    catch (SQLException ex) {
      ResourceException re = new EISSystemException(ex.getMessage());
      re.setLinkedException(ex);
      throw re;
    }
    finally {
      try {
        con.setAutoCommit(true);
      }
      catch (Exception ex) {
      }
    }
  }

}
