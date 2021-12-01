/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */


package com.tencent.angel.master;

import com.tencent.angel.master.app.AMContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Heart monitor for all modules
 */
public class HeartbeatMonitor extends AbstractService {
  private static final Log LOG = LogFactory.getLog(HeartbeatMonitor.class);
  private final AMContext context;
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  /**
   * Heartbeat timeout checker
   */
  private volatile Thread timeOutChecker;

  /**
   * Construct the service.
   */
  public HeartbeatMonitor(AMContext context) {
    super("Heartbeat monitor");
    this.context = context;
  }

  @Override protected void serviceStart() throws Exception {
    timeOutChecker = new Thread(new Runnable() {
      @SuppressWarnings("unchecked") @Override public void run() {
        while (!stopped.get() && !Thread.currentThread().isInterrupted()) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            LOG.warn(Thread.currentThread().getName() + " is interupted");
          }

          // Check Workers
          if (context.getWorkerManager() != null) {
            context.getWorkerManager().checkHBTimeOut();
          }

          // Check PSS
          if (context.getParameterServerManager() != null) {
            context.getParameterServerManager().checkHBTimeOut();
          }

          // Check Clients
          if (context.getClientManager() != null) {
            context.getClientManager().checkHBTimeOut();
          }

          // Check PS Clients
          if (context.getPSAgentManager() != null) {
            context.getPSAgentManager().checkHBTimeOut();
          }
        }
      }
    });
    timeOutChecker.setName("Heartbeat Timeout checker");
    timeOutChecker.start();
  }

  @Override protected void serviceInit(Configuration conf) throws Exception {
    super.serviceInit(conf);
  }

  @Override protected void serviceStop() throws Exception {
    if (stopped.getAndSet(true)) {
      return;
    }
    if (timeOutChecker != null) {
      timeOutChecker.interrupt();
      timeOutChecker = null;
    }

    super.serviceStop();
    LOG.info("Heartbeat monitor stopped");
  }
}
