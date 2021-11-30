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

package org.apache.fluo.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FluoThreadFactory implements ThreadFactory {

  private static AtomicInteger poolCount = new AtomicInteger();
  private AtomicInteger threadCount = new AtomicInteger();

  private String poolName;
  private int poolId;

  public FluoThreadFactory(String poolName) {
    this.poolName = poolName;
    this.poolId = poolCount.incrementAndGet();
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r);
    String name =
        String.format("Fluo-%04d-%03d-%s", poolId, threadCount.incrementAndGet(), poolName);
    t.setName(name);
    t.setDaemon(true);
    return t;
  }
}
