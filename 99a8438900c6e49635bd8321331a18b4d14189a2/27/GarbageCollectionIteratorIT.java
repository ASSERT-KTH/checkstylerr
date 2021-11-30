/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.fluo.integration.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.PartialKey;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.fluo.accumulo.format.FluoFormatter;
import org.apache.fluo.accumulo.util.ColumnConstants;
import org.apache.fluo.accumulo.util.ZookeeperPath;
import org.apache.fluo.accumulo.util.ZookeeperUtil;
import org.apache.fluo.api.client.TransactionBase;
import org.apache.fluo.api.data.Column;
import org.apache.fluo.core.impl.TransactionImpl.CommitData;
import org.apache.fluo.core.impl.TransactorNode;
import org.apache.fluo.integration.BankUtil;
import org.apache.fluo.integration.ITBaseImpl;
import org.apache.fluo.integration.TestTransaction;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Tests GarbageCollectionIterator class
 */
public class GarbageCollectionIteratorIT extends ITBaseImpl {
  @Rule
  public Timeout globalTimeout = Timeout.seconds(getTestTimeout());

  private void waitForGcTime(long expectedTime) throws Exception {
    env.getSharedResources().getTimestampTracker().updateZkNode();
    long oldestTs = ZookeeperUtil.getGcTimestamp(config.getAppZookeepers());
    while (oldestTs < expectedTime) {
      Thread.sleep(500);
      oldestTs = ZookeeperUtil.getGcTimestamp(config.getAppZookeepers());
    }
  }


  public void testVerifyAfterGC() throws Exception {

    final TestTransaction tx1 = new TestTransaction(env);
    BankUtil.setBalance(tx1, "bob", 10);
    BankUtil.setBalance(tx1, "joe", 20);
    BankUtil.setBalance(tx1, "jill", 60);
    tx1.done();

    BankUtil.transfer(env, "joe", "jill", 1);
    BankUtil.transfer(env, "joe", "bob", 1);
    BankUtil.transfer(env, "bob", "joe", 2);
    BankUtil.transfer(env, "jill", "joe", 2);

    final TestTransaction tx2 = new TestTransaction(env);
    waitForGcTime(tx2.getStartTimestamp());

    long oldestTs = ZookeeperUtil.getGcTimestamp(config.getAppZookeepers());
    Assert.assertEquals(tx2.getStartTs(), oldestTs);

    // Force a garbage collection
    aClient.tableOperations().flush(table, null, null, true);

    verify(oldestTs);

    final TestTransaction tx3 = new TestTransaction(env);
    Assert.assertEquals(9, BankUtil.getBalance(tx3, "bob"));
    Assert.assertEquals(22, BankUtil.getBalance(tx3, "joe"));
    Assert.assertEquals(59, BankUtil.getBalance(tx3, "jill"));
    tx3.done();
    tx2.done();
  }


  public void testDeletedDataIsDropped() throws Exception {

    final Column docUri = new Column("doc", "uri");

    final TestTransaction tx1 = new TestTransaction(env);
    tx1.set("001", docUri, "file:///abc.txt");
    tx1.done();

    final TestTransaction tx2 = new TestTransaction(env);

    final TestTransaction tx3 = new TestTransaction(env);
    tx3.delete("001", docUri);
    tx3.done();

    final TestTransaction tx4 = new TestTransaction(env);

    waitForGcTime(tx2.getStartTimestamp());

    // Force a garbage collection
    aClient.tableOperations().compact(table, null, null, true, true);

    Assert.assertEquals("file:///abc.txt", tx2.gets("001", docUri));

    tx2.done();

    Assert.assertNull(tx4.gets("001", docUri));

    waitForGcTime(tx4.getStartTimestamp());
    aClient.tableOperations().compact(table, null, null, true, true);

    Assert.assertNull(tx4.gets("001", docUri));

    Scanner scanner = aClient.createScanner(table, Authorizations.EMPTY);
    Assert.assertEquals(0, Iterables.size(scanner));

    tx4.done();
  }


  public void testRolledBackDataIsDropped() throws Exception {

    Column col1 = new Column("fam1", "q1");
    Column col2 = new Column("fam1", "q2");

    TransactorNode t2 = new TransactorNode(env);
    final TestTransaction tx2 = new TestTransaction(env, t2);

    for (int r = 0; r < 10; r++) {
      tx2.set(r + "", col1, "1" + r + "0");
      tx2.set(r + "", col2, "1" + r + "1");
    }

    CommitData cd = tx2.createCommitData();
    Assert.assertTrue(tx2.preCommit(cd));

    t2.close();

    // rollback data
    final TestTransaction tx3 = new TestTransaction(env, t2);
    for (int r = 0; r < 10; r++) {
      tx3.gets(r + "", col1);
      tx3.gets(r + "", col2);
    }
    tx3.done();

    Assert.assertEquals(20, countInTable("-LOCK"));
    Assert.assertEquals(20, countInTable("-DEL_LOCK"));
    Assert.assertEquals(20, countInTable("-DATA"));

    // flush should drop locks and data
    aClient.tableOperations().flush(table, null, null, true);

    Assert.assertEquals(0, countInTable("-LOCK"));
    Assert.assertEquals(20, countInTable("-DEL_LOCK"));
    Assert.assertEquals(0, countInTable("-DATA"));

    // compact should drop all del locks except for primary
    aClient.tableOperations().compact(table, null, null, true, true);

    Assert.assertEquals(0, countInTable("-LOCK"));
    Assert.assertEquals(1, countInTable("-DEL_LOCK"));
    Assert.assertEquals(0, countInTable("-DATA"));
  }

  private void increment(TransactionBase tx, String row, Column col) {
    int count = Integer.parseInt(tx.gets(row, col, "0"));
    tx.set(row, col, count + 1 + "");
  }

  @Test(timeout = 60000)
  public void testReadLocks() throws Exception {

    final Column altIdCol = new Column("info", "altId");

    final TestTransaction tx1 = new TestTransaction(env);
    for (int i = 0; i < 10; i++) {
      tx1.set(String.format("n:%03d", i), altIdCol, "" + (19 * (1 + i)));
    }

    tx1.done();

    for (int i = 0; i < 50; i++) {
      String row = String.format("n:%03d", i % 10);

      final TestTransaction tx = new TestTransaction(env);
      String altId = tx.withReadLock().gets(row, altIdCol);

      increment(tx, "a:" + altId, new Column("count", row));

      tx.done();
    }

    Assert.assertEquals(50, countInTable("-DEL_RLOCK"));
    Assert.assertEquals(50, countInTable("-RLOCK"));

    final TestTransaction tx2 = new TestTransaction(env);
    for (int i = 0; i < 10; i++) {
      String row = String.format("n:%03d", i);
      String newAltId = (13 * (i + 1)) + "";
      String currAltId = tx2.gets(row, altIdCol);


      tx2.set(row, altIdCol, newAltId);

      String count = tx2.gets("a:" + currAltId, new Column("count", row));
      tx2.set("a:" + newAltId, new Column("count", row), count);
      tx2.delete("a:" + currAltId, new Column("count", row));
    }

    tx2.done();

    // all read locks should be garbage collected because of the writes after the read locks
    aClient.tableOperations().compact(table, null, null, true, true);

    Assert.assertEquals(0, countInTable("-DEL_RLOCK"));
    Assert.assertEquals(0, countInTable("-RLOCK"));

    for (int i = 0; i < 50; i++) {
      String row = String.format("n:%03d", i % 10);

      final TestTransaction tx = new TestTransaction(env);
      String altId = tx.withReadLock().gets(row, altIdCol);

      increment(tx, "a:" + altId, new Column("count", row));

      tx.done();
    }

    final TestTransaction tx3 = new TestTransaction(env);
    for (int i = 0; i < 10; i++) {
      String row = String.format("n:%03d", i);
      String currAltId = tx3.gets(row, altIdCol);
      Assert.assertEquals("10", tx3.gets("a:" + currAltId, new Column("count", row)));
    }

    tx3.done();

    waitForGcTime(tx3.getStartTimestamp());
    aClient.tableOperations().compact(table, null, null, true, true);


    // all read locks older than GC time should be dropped
    Assert.assertEquals(0, countInTable("-DEL_RLOCK"));
    Assert.assertEquals(0, countInTable("-RLOCK"));
  }

  private int countInTable(String str) throws TableNotFoundException {
    int count = 0;
    Scanner scanner = aClient.createScanner(table, Authorizations.EMPTY);
    for (String e : Iterables.transform(scanner, FluoFormatter::toString)) {
      if (e.contains(str)) {
        count++;
      }
    }

    return count;
  }

  @Test
  public void testGetOldestTimestamp() throws Exception {
    // we are expecting an error in this test
    final Level curLevel = Logger.getLogger(ZookeeperUtil.class).getLevel();
    Logger.getLogger(ZookeeperUtil.class).setLevel(Level.FATAL);

    // verify that oracle initial current ts
    Assert.assertEquals(0, ZookeeperUtil.getGcTimestamp(config.getAppZookeepers()));
    // delete the oracle current timestamp path
    env.getSharedResources().getCurator().delete().forPath(ZookeeperPath.ORACLE_GC_TIMESTAMP);
    // verify that oldest possible is returned
    Assert.assertEquals(ZookeeperUtil.OLDEST_POSSIBLE,
        ZookeeperUtil.getGcTimestamp(config.getAppZookeepers()));

    // set level back
    Logger.getLogger(ZookeeperUtil.class).setLevel(curLevel);
  }

  /**
   * Verifies that older versions of data are newer than given timestamp
   *
   */
  private void verify(long oldestTs) throws TableNotFoundException {
    Scanner scanner = aClient.createScanner(table, Authorizations.EMPTY);

    Iterator<Entry<Key, Value>> iter = scanner.iterator();

    Entry<Key, Value> prev = null;
    int numWrites = 0;
    while (iter.hasNext()) {
      Entry<Key, Value> entry = iter.next();

      if ((prev == null)
          || !prev.getKey().equals(entry.getKey(), PartialKey.ROW_COLFAM_COLQUAL_COLVIS)) {
        numWrites = 0;
      }

      long colType = entry.getKey().getTimestamp() & ColumnConstants.PREFIX_MASK;
      long ts = entry.getKey().getTimestamp() & ColumnConstants.TIMESTAMP_MASK;

      if (colType == ColumnConstants.WRITE_PREFIX) {
        numWrites++;
        if (numWrites > 1) {
          Assert.assertTrue("Extra write had ts " + ts + " < " + oldestTs, ts >= oldestTs);
        }
      }
      prev = entry;
    }
  }
}
