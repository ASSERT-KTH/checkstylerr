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

package org.apache.fluo.integration;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.apache.accumulo.minicluster.MiniAccumuloConfig;
import org.apache.accumulo.minicluster.MiniAccumuloInstance;
import org.apache.commons.io.FileUtils;
import org.apache.fluo.api.client.FluoClient;
import org.apache.fluo.api.client.Snapshot;
import org.apache.fluo.api.config.FluoConfiguration;
import org.apache.fluo.api.data.RowColumnValue;
import org.apache.fluo.api.observer.ObserverProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base Integration Test class
 */
public class ITBase {

  protected static final String USER = "root";
  protected static final String PASSWORD = "ITSecret";
  protected static final String TABLE_BASE = "table";
  protected static final String IT_INSTANCE_NAME_PROP =
      FluoConfiguration.FLUO_PREFIX + ".it.instance.name";
  protected static final String IT_INSTANCE_CLEAR_PROP =
      FluoConfiguration.FLUO_PREFIX + ".it.instance.clear";

  protected static String instanceName;
  protected static Connector conn;
  protected static Instance miniAccumulo;
  private static MiniAccumuloCluster cluster;
  private static boolean startedCluster = false;

  protected static FluoConfiguration config;
  protected static FluoClient client;

  private static AtomicInteger tableCounter = new AtomicInteger(1);
  protected static AtomicInteger testCounter = new AtomicInteger();

  private static final long JUNIT_TIMEOUT_SECONDS = 120;

  /**
   * Gets the duration a test will run before timing out under the JUnit rule. This value is in
   * seconds.
   *
   * @return long representation of the time in seconds
   * @since 1.2.0
   */
  public static long getTestTimeout() {
    return JUNIT_TIMEOUT_SECONDS;
  }

  @BeforeClass
  public static void setUpAccumulo() throws Exception {
    instanceName = System.getProperty(IT_INSTANCE_NAME_PROP, "it-instance-default");
    File instanceDir = new File("target/accumulo-maven-plugin/" + instanceName);
    boolean instanceClear =
        System.getProperty(IT_INSTANCE_CLEAR_PROP, "true").equalsIgnoreCase("true");
    if (instanceDir.exists() && instanceClear) {
      FileUtils.deleteDirectory(instanceDir);
    }
    if (!instanceDir.exists()) {
      MiniAccumuloConfig cfg = new MiniAccumuloConfig(instanceDir, PASSWORD);
      cfg.setInstanceName(instanceName);
      cluster = new MiniAccumuloCluster(cfg);
      cluster.start();
      startedCluster = true;
    }
    miniAccumulo = new MiniAccumuloInstance(instanceName, instanceDir);
    conn = miniAccumulo.getConnector(USER, new PasswordToken(PASSWORD));
  }

  protected Class<? extends ObserverProvider> getObserverProviderClass() {
    return null;
  }

  protected void setupObservers(FluoConfiguration fc) {
    Class<? extends ObserverProvider> ofc = getObserverProviderClass();
    if (ofc != null) {
      fc.setObserverProvider(ofc);
    }
  }

  public String getCurTableName() {
    return TABLE_BASE + tableCounter.get();
  }

  public String getNextTableName() {
    return TABLE_BASE + tableCounter.incrementAndGet();
  }

  protected void printSnapshot(Consumer<String> out) throws Exception {
    try (Snapshot s = client.newSnapshot()) {
      out.accept("== snapshot start ==");

      for (RowColumnValue rcv : s.scanner().build()) {
        out.accept(rcv.getRow() + " " + rcv.getColumn() + "\t" + rcv.getValue());
      }

      out.accept("=== snapshot end ===");
    }
  }

  protected void printSnapshot() throws Exception {
    printSnapshot(System.out::println);
  }

  @AfterClass
  public static void tearDownAccumulo() throws Exception {
    if (startedCluster) {
      cluster.stop();
    }
  }
}
