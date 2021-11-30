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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.apache.fluo.core.impl.Environment;
import org.apache.fluo.core.oracle.OracleClient;
import org.apache.fluo.core.oracle.OracleServer;
import org.apache.fluo.core.util.HostUtil;
import org.apache.fluo.core.util.PortUtils;
import org.apache.fluo.integration.ITBaseImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OracleIT extends ITBaseImpl {

  private static final String THRIFT_SERVER_LOGGER_NAME =
      "org.apache.fluo.core.shaded.thrift.server";

  @Rule
  public Timeout globalTimeout = Timeout.seconds(getTestTimeout());

  private Level curLevel;

  @Test
  public void testRestart() throws Exception {
    OracleClient client = env.getSharedResources().getOracleClient();

    long ts1 = client.getStamp().getTxTimestamp();
    long ts2 = client.getStamp().getTxTimestamp();

    oserver.stop();
    oserver.start();

    long ts3 = client.getStamp().getTxTimestamp();
    long ts4 = client.getStamp().getTxTimestamp();

    assertTrue(ts1 + " " + ts2, ts1 < ts2);
    assertTrue(ts2 + " " + ts3, ts2 < ts3);
    assertTrue(ts3 + " " + ts4, ts3 < ts4);
  }

  private static class TimestampFetcher implements Runnable {
    private int numToGet;
    private Environment env;
    private List<Long> output;
    private CountDownLatch cdl;

    TimestampFetcher(int numToGet, Environment env, List<Long> output, CountDownLatch cdl) {
      this.numToGet = numToGet;
      this.env = env;
      this.output = output;
      this.cdl = cdl;
    }

    @Override
    public void run() {
      OracleClient oclient = env.getSharedResources().getOracleClient();

      for (int i = 0; i < numToGet; i++) {
        try {
          output.add(oclient.getStamp().getTxTimestamp());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      cdl.countDown();
    }
  }

  @Before
  public void disableLogger() {
    curLevel = Logger.getLogger(THRIFT_SERVER_LOGGER_NAME).getLevel();
    Logger.getLogger(THRIFT_SERVER_LOGGER_NAME).setLevel(Level.FATAL);
  }

  @After
  public void enableLogger() {
    Logger.getLogger(THRIFT_SERVER_LOGGER_NAME).setLevel(curLevel);
  }

  /**
   * Test that bogus input into the oracle server doesn't cause an OOM exception. This essentially
   * tests for THRIFT-602
   */
  @Test
  public void bogusDataTest() throws Exception {

    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(HostUtil.getHostName(), oserver.getPort()));
    OutputStream outstream = socket.getOutputStream();
    try (PrintWriter out = new PrintWriter(outstream)) {
      out.print("abcd");
      out.flush();
    }

    socket.close();

    OracleClient client = env.getSharedResources().getOracleClient();

    assertEquals(2, client.getStamp().getTxTimestamp());
  }

  @Test
  public void threadTest() throws Exception {

    int numThreads = 20;
    int numTimes = 100;

    List<Long> output = Collections.synchronizedList(new ArrayList<Long>());
    ExecutorService tpool = Executors.newFixedThreadPool(numThreads);
    CountDownLatch cdl = new CountDownLatch(numThreads);

    for (int i = 0; i < numThreads; i++) {
      tpool.execute(new TimestampFetcher(numTimes, env, output, cdl));
    }

    cdl.await();

    TreeSet<Long> ts1 = new TreeSet<>(output);

    assertEquals(numThreads * numTimes, ts1.size());

    cdl = new CountDownLatch(numThreads);
    output.clear();

    for (int i = 0; i < numThreads; i++) {
      tpool.execute(new TimestampFetcher(numTimes, env, output, cdl));
    }

    cdl.await();

    TreeSet<Long> ts2 = new TreeSet<>(output);

    assertEquals(numThreads * numTimes, ts2.size());
    assertTrue(ts1.last() < ts2.first());

    tpool.shutdown();
  }

  /**
   * If multiple {@link org.apache.fluo.core.oracle.OracleServer} instances are competing leadership
   * and fail, the {@link OracleClient} should failover to them as they go down and serve up new
   * blocks of timestamps.
   */
  @Test
  public void failover_newTimestampRequested() throws Exception {

    sleepUntil(oserver::isConnected);

    int port2 = PortUtils.getRandomFreePort();
    int port3 = PortUtils.getRandomFreePort();

    TestOracle oserver2 = createExtraOracle(port2);
    TestOracle oserver3 = createExtraOracle(port3);

    oserver2.start();
    sleepUntil(oserver2::isConnected);

    oserver3.start();
    sleepUntil(oserver3::isConnected);

    OracleClient client = env.getSharedResources().getOracleClient();

    long timestamp;
    for (long i = 2; i <= 7; i++) {
      timestamp = client.getStamp().getTxTimestamp();
      assertEquals(i, timestamp);
    }

    assertTrue(client.getOracle().endsWith(Integer.toString(oserver.getPort())));

    oserver.stop();
    sleepWhile(oserver::isConnected);
    sleepUntil(oserver2::isLeader);

    assertEquals(1002, client.getStamp().getTxTimestamp());
    assertTrue(client.getOracle().endsWith(Integer.toString(port2)));

    oserver2.stop();
    sleepWhile(oserver2::isConnected);
    oserver2.close();

    sleepUntil(oserver3::isLeader);

    assertEquals(2002, client.getStamp().getTxTimestamp());
    assertTrue(client.getOracle().endsWith(Integer.toString(port3)));

    oserver3.stop();
    oserver3.close();
  }

  /**
   * If an {@link OracleServer} goes away and comes back, the client should automatically reconnect
   * and start a new block of timestamps (making sure that no timestamp should ever go backwards).
   */
  @Test
  public void singleOracle_goesAwayAndComesBack() throws Exception {

    sleepUntil(oserver::isConnected);

    OracleClient client = env.getSharedResources().getOracleClient();

    long timestamp;
    for (long i = 2; i <= 7; i++) {
      timestamp = client.getStamp().getTxTimestamp();
      assertEquals(i, timestamp);
    }

    oserver.stop();
    sleepWhile(oserver::isConnected);

    while (client.getOracle() != null) {
      Thread.sleep(100);
    }

    assertNull(client.getOracle());

    oserver.start();
    sleepUntil(oserver::isConnected);

    assertEquals(1002, client.getStamp().getTxTimestamp());

    assertTrue(client.getOracle().endsWith(Integer.toString(oserver.getPort())));

    oserver.stop();
  }

  @Test
  public void threadFailoverTest() throws Exception {

    int numThreads = 20;
    int numTimes = 100;

    List<Long> output = Collections.synchronizedList(new ArrayList<Long>());
    ExecutorService tpool = Executors.newFixedThreadPool(numThreads);
    CountDownLatch cdl = new CountDownLatch(numThreads);

    int port2 = PortUtils.getRandomFreePort();
    int port3 = PortUtils.getRandomFreePort();

    TestOracle oserver2 = createExtraOracle(port2);

    oserver2.start();
    sleepUntil(oserver2::isConnected);

    TestOracle oserver3 = createExtraOracle(port3);

    oserver3.start();
    sleepUntil(oserver3::isConnected);

    for (int i = 0; i < numThreads; i++) {
      tpool.execute(new TimestampFetcher(numTimes, env, output, cdl));

      if (i == 10) {
        oserver.stop();
      }
    }

    cdl.await();

    TreeSet<Long> ts1 = new TreeSet<>(output);

    assertEquals(numThreads * numTimes, ts1.size());

    cdl = new CountDownLatch(numThreads);
    output.clear();

    for (int i = 0; i < numThreads; i++) {
      tpool.execute(new TimestampFetcher(numTimes, env, output, cdl));

      if (i == 5) {
        oserver2.stop();
      }
    }
    oserver2.close();

    cdl.await();

    TreeSet<Long> ts2 = new TreeSet<>(output);

    assertEquals(numThreads * numTimes, ts2.size());
    assertTrue(ts1.last() < ts2.first());

    tpool.shutdown();
    oserver3.stop();
    oserver3.close();
  }

  private void sleepUntil(Supplier<Boolean> condition) throws InterruptedException {
    sleepWhile(() -> !condition.get());
  }

  private void sleepWhile(Supplier<Boolean> condition) throws InterruptedException {
    while (condition.get()) {
      Thread.sleep(100);
    }
  }

}
