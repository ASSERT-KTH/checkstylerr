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


package com.tencent.angel.master.ps;

import com.tencent.angel.AngelDeployMode;
import com.tencent.angel.common.location.Location;
import com.tencent.angel.conf.AngelConf;
import com.tencent.angel.master.app.AMContext;
import com.tencent.angel.master.app.AppEvent;
import com.tencent.angel.master.app.AppEventType;
import com.tencent.angel.master.app.InternalErrorEvent;
import com.tencent.angel.master.ps.attempt.PSAttemptDiagnosticsUpdateEvent;
import com.tencent.angel.master.ps.attempt.PSAttemptEvent;
import com.tencent.angel.master.ps.attempt.PSAttemptEventType;
import com.tencent.angel.master.ps.ps.AMParameterServer;
import com.tencent.angel.master.ps.ps.AMParameterServerEvent;
import com.tencent.angel.master.ps.ps.AMParameterServerEventType;
import com.tencent.angel.ps.PSAttemptId;
import com.tencent.angel.ps.ParameterServerId;
import com.tencent.angel.ps.server.data.PSLocation;
import com.tencent.angel.utils.StringUtils;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Parameter server manager, it managers a group of
 * {@link com.tencent.angel.master.ps.ps.AMParameterServer}. It is responsible for starting all the
 * parameter servers. If all the workers have completed the model training, it will inform the
 * parameter servers to write models to the corresponding file, and finally merge them.
 */
public class ParameterServerManager extends AbstractService
  implements EventHandler<ParameterServerManagerEvent> {

  private static final Log LOG = LogFactory.getLog(ParameterServerManager.class);
  private final AMContext context;

  /**
   * parameter server number
   */
  private final int psNumber;

  /**
   * If we need to suggest physical machines which the parameter servers will running on, we can
   * specify the physical machine ip list
   */
  private final String[] ips;

  /**
   * the amount of resources requested for each parameter server
   */
  private final Resource psResource;

  /**
   * the resource priority for parameter servers
   */
  private final Priority priority;

  /**
   * parameter server id to parameter server management unit map
   */
  private final Map<ParameterServerId, AMParameterServer> psMap;

  /**
   * The parameter server collection that has completed the commit operation
   */
  private final Set<ParameterServerId> committedPs;

  /**
   * whether you can start commit operation
   */
  private final AtomicBoolean canCommit;

  /**
   * need commit matrices
   */
  private volatile List<Integer> needCommitMatrixIds;

  /**
   * parameter server id to attempt index map, it use to master recover
   */
  private final Map<ParameterServerId, Integer> psIdToAttemptIndexMap;

  private final AMPSFailedReport report;

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  /**
   * parameter server attempt id to last heartbeat timestamp map
   */
  private final ConcurrentHashMap<PSAttemptId, Long> psLastHeartbeatTS = new ConcurrentHashMap<>();

  /**
   * parameter server attempt id queue for kubernetes allocator
   */
  private final LinkedBlockingDeque<PSAttemptId> psAttemptIdBlockingQueue = new LinkedBlockingDeque<>();

  /**
   * parameter server heartbeat timeout value in millisecond
   */
  private final long psTimeOutMS;

  private final boolean useMiniBatch;
  private final int miniBatchSize;
  private final int requestIntervalMS;


  public ParameterServerManager(AMContext context,
    Map<ParameterServerId, Integer> psIdToAttemptIndexMap) {
    super("PS Manager");
    this.context = context;
    this.psIdToAttemptIndexMap = psIdToAttemptIndexMap;
    Configuration conf = context.getConf();
    String ipListStr = conf.get(AngelConf.ANGEL_PS_IP_LIST);
    if (ipListStr != null) {
      ips = ipListStr.split(",");
      psNumber = ips.length;
    } else {
      ips = null;
      psNumber = conf.getInt(AngelConf.ANGEL_PS_NUMBER, AngelConf.DEFAULT_ANGEL_PS_NUMBER);
    }

    int psServerMemory =
      conf.getInt(AngelConf.ANGEL_PS_MEMORY_GB, AngelConf.DEFAULT_ANGEL_PS_MEMORY_GB) * 1024;

    int psServerVcores =
      conf.getInt(AngelConf.ANGEL_PS_CPU_VCORES, AngelConf.DEFAULT_ANGEL_PS_CPU_VCORES);

    int psPriority = conf.getInt(AngelConf.ANGEL_PS_PRIORITY, AngelConf.DEFAULT_ANGEL_PS_PRIORITY);

    useMiniBatch = conf.getBoolean(AngelConf.ANGEL_PS_REQUEST_RESOURCE_USE_MINIBATCH,
        AngelConf.DEFAULT_ANGEL_PS_REQUEST_RESOURCE_USE_MINIBATCH);

    miniBatchSize = conf.getInt(AngelConf.ANGEL_PS_REQUEST_RESOURCE_MINIBATCH_SIZE,
        AngelConf.DEFAULT_ANGEL_PS_REQUEST_RESOURCE_MINIBATCH_SIZE);

    requestIntervalMS = conf.getInt(AngelConf.ANGEL_PS_REQUEST_RESOURCE_MINIBATCH_INTERVAL_MS,
        AngelConf.DEFAULT_ANGEL_PS_REQUEST_RESOURCE_MINIBATCH_INTERVAL_MS);

    psTimeOutMS = conf.getLong(AngelConf.ANGEL_PS_HEARTBEAT_TIMEOUT_MS,
      AngelConf.DEFAULT_ANGEL_PS_HEARTBEAT_TIMEOUT_MS);

    psResource = Resource.newInstance(psServerMemory, psServerVcores);
    priority = RecordFactoryProvider.getRecordFactory(null).newRecordInstance(Priority.class);
    priority.setPriority(psPriority);

    psMap = new HashMap<>();
    committedPs = new HashSet<>();

    canCommit = new AtomicBoolean(false);
    report = new AMPSFailedReport();
  }

  @Override protected void serviceStart() throws Exception {

  }

  @Override protected void serviceStop() throws Exception {
    if (stopped.getAndSet(true)) {
      return;
    }
    super.serviceStop();
    LOG.info("ParameterServerManager stopped");
  }

  /**
   * Init all PS
   */
  public void init() {
    for (int i = 0; i < psNumber; i++) {
      ParameterServerId id = new ParameterServerId(i);
      AMParameterServer server = null;
      if (ips != null) {
        server = new AMParameterServer(ips[i], id, context);
      } else {
        server = new AMParameterServer(id, context);
      }

      if (psIdToAttemptIndexMap != null && psIdToAttemptIndexMap.containsKey(id)) {
        server.setNextAttemptNumber(psIdToAttemptIndexMap.get(id));
      }
      psMap.put(id, server);
    }
  }

  /**
   * Start all PS
   */
  public void startAllPS() {
    if(useMiniBatch) {
      Thread requestThread = new RequestThread();
      requestThread.setName("resource-requester");
      requestThread.start();
    } else {
      for (Map.Entry<ParameterServerId, AMParameterServer> entry : psMap.entrySet()) {
        entry.getValue()
            .handle(new AMParameterServerEvent(AMParameterServerEventType.PS_SCHEDULE, entry.getKey()));
      }
    }
  }

  class RequestThread extends Thread {
    @Override
    public void run() {
      int index = 0;
      for (Map.Entry<ParameterServerId, AMParameterServer> entry : psMap.entrySet()) {
        entry.getValue()
            .handle(new AMParameterServerEvent(AMParameterServerEventType.PS_SCHEDULE, entry.getKey()));
        index++;
        if(index % miniBatchSize == 0) {
          try {
            Thread.sleep(requestIntervalMS);
          } catch (InterruptedException e) {
            if(!stopped.get()) {
              LOG.error("interrupt where request resource ", e);
            }
          }
        }
      }
    }
  }

  /**
   * get parameter servers map
   *
   * @return Map<ParameterServerId, AMParameterServer> parameter servers map
   */
  public Map<ParameterServerId, AMParameterServer> getParameterServerMap() {
    return psMap;
  }

  /**
   * parameter server attempt id queue
   * @return LinkedBlockingDeque<PSAttemptId>
   */

  public LinkedBlockingDeque<PSAttemptId> getPsAttemptIdBlockingQueue() {
    return psAttemptIdBlockingQueue;
  }

  @Override public void handle(ParameterServerManagerEvent event) {
    LOG.debug("Processing event type " + event.getType());
    switch (event.getType()) {
      case COMMIT: {
        LOG.info("set canCommit to true.");
        canCommit.set(true);
        needCommitMatrixIds = ((CommitEvent) event).getNeedCommitMatrixIds();
        break;
      }

      case PARAMETERSERVER_DONE: {
        commitSuccess(event);
        break;
      }

      case PARAMETERSERVER_FAILED: {
        psFailed(event);
        break;
      }

      case PARAMETERSERVER_KILLED: {
        psKilled(event);
        break;
      }

      default:
        break;
    }
  }

  /**
   * Check whether we can start the commit operation
   *
   * @return boolean true indicates that a commit operation can be performed
   */
  public boolean psCanCommit() {
    return canCommit.get();
  }

  /**
   * get parameter server manager unit use id
   *
   * @param id parameter server id
   * @return AMParameterServer parameter server manager unit
   */
  public AMParameterServer getParameterServer(ParameterServerId id) {
    return psMap.get(id);
  }

  /**
   * get parameter server number
   *
   * @return int parameter server number
   */
  public int getPsNumber() {
    return psNumber;
  }

  /**
   * get parameter server resource allocation
   *
   * @return Resource parameter server resource allocation
   */
  public Resource getPsResource() {
    return psResource;
  }

  /**
   * get parameter server resource priority
   *
   * @return Priority parameter server resource priority
   */
  public Priority getPriority() {
    return priority;
  }

  @SuppressWarnings("unchecked") private void psKilled(ParameterServerManagerEvent event) {
    context.getEventHandler().handle(new AppEvent(context.getApplicationId(), AppEventType.KILL));
  }

  @SuppressWarnings("unchecked") private void psFailed(ParameterServerManagerEvent event) {
    List<String> diagnostics =
      context.getParameterServerManager().getParameterServer(event.getPsId()).getDiagnostics();
    StringBuilder sb = new StringBuilder();
    sb.append(StringUtils.join("\n", diagnostics));
    context.getEventHandler()
      .handle(new InternalErrorEvent(context.getApplicationId(), sb.toString()));
  }

  private void commitSuccess(ParameterServerManagerEvent event) {
    committedPs.add(event.getPsId());
    //if all parameter server complete commit, master can commit now
    if (committedPs.size() == psNumber) {
      commit();
    }
  }

  private void commit() {
  }


  /**
   * Get the matrices that need commit.
   *
   * @return List<Integer> matrices that need commit.
   */
  public List<Integer> getNeedCommitMatrixIds() {
    return needCommitMatrixIds;
  }

  /**
   * Update ps failed counters
   *
   * @param counters ps failed counters
   */
  public void psFailedReports(Map<PSLocation, Integer> counters) {
    report.psFailedReports(counters);
  }

  public void psFailedReport(PSLocation psLoc) {
    //restartPS(psLoc);
  }

  private void restartPS(PSLocation psLoc) {
    getParameterServer(psLoc.psId).restart(psLoc);
  }

  public void checkHBTimeOut() {
    //check whether parameter server heartbeat timeout
    Iterator<Map.Entry<PSAttemptId, Long>> psIt = psLastHeartbeatTS.entrySet().iterator();
    Map.Entry<PSAttemptId, Long> psEntry;
    long currentTs = System.currentTimeMillis();
    while (psIt.hasNext()) {
      psEntry = psIt.next();
      if (currentTs - psEntry.getValue() > psTimeOutMS) {
        LOG.error(psEntry.getKey() + " heartbeat timeout!!!");
        context.getEventHandler()
          .handle(new PSAttemptDiagnosticsUpdateEvent("heartbeat timeout", psEntry.getKey()));

        context.getEventHandler()
          .handle(new PSAttemptEvent(PSAttemptEventType.PA_FAILMSG, psEntry.getKey()));
        psIt.remove();
      }
    }
  }

  /**
   * PS attempt register
   *
   * @param psAttemptId PS attempt id
   */
  public void register(PSAttemptId psAttemptId) {
    LOG.info("PS " + psAttemptId + " is registered in monitor!");
    psLastHeartbeatTS.put(psAttemptId, System.currentTimeMillis());
    if (context.getDeployMode() == AngelDeployMode.KUBERNETES) {
      psAttemptIdBlockingQueue.add(psAttemptId);
    }
  }

  /**
   * PS attempt unregister
   *
   * @param psAttemptId PS attempt id
   */
  public void unRegister(PSAttemptId psAttemptId) {
    LOG.info("PS " + psAttemptId + " is finished,  delete it in monitor!");
    psLastHeartbeatTS.remove(psAttemptId);
  }

  /**
   * Is PS attempt alive
   *
   * @param psAttemptId PS attempt id
   * @return true mean alive
   */
  public boolean isAlive(PSAttemptId psAttemptId) {
    return psLastHeartbeatTS.containsKey(psAttemptId);
  }

  /**
   * Update PS attempt latest heartbeat timestamp
   *
   * @param psAttemptId PS attempt id
   */
  public void alive(PSAttemptId psAttemptId) {
    psLastHeartbeatTS.put(psAttemptId, System.currentTimeMillis());
  }

  /**
   * Is PS attempt failed
   *
   * @param psLoc PS id and PS location
   * @return true means failed
   */
  public boolean checkFailed(PSLocation psLoc) {
    Location loc = context.getLocationManager().getPsLocation(psLoc.psId);
    if (loc == null || !loc.equals(psLoc.loc)) {
      return true;
    } else {
      return false;
    }
  }
}
