/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.loadbalance;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceInstancePing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 *  Add special stats that com.netflix.loadbalancer.LoadBalancerStats not provided
 */
public class ServiceCombLoadBalancerStats {
  private final static Logger LOGGER = LoggerFactory.getLogger(ServiceCombLoadBalancerStats.class);

  private static final int SERVERSTATS_EXPIRE_MINUTES = 10;

  private static final long TIMER_INTERVAL_MILLIS = 10000;

  private static final LoadingCache<ServiceCombServer, ServiceCombServerStats> SERVER_STATES_CACHE =
      CacheBuilder.newBuilder()
          .expireAfterAccess(SERVERSTATS_EXPIRE_MINUTES, TimeUnit.MINUTES)
          .removalListener(new RemovalListener<ServiceCombServer, ServiceCombServerStats>() {
            @Override
            public void onRemoval(RemovalNotification<ServiceCombServer, ServiceCombServerStats> notification) {
              LOGGER.info("stats of instance {} removed.", notification.getKey().getInstance().getInstanceId());
            }
          })
          .build(
              new CacheLoader<ServiceCombServer, ServiceCombServerStats>() {
                public ServiceCombServerStats load(ServiceCombServer server) {
                  return new ServiceCombServerStats();
                }
              });

  public static final ServiceCombLoadBalancerStats INSTANCE = new ServiceCombLoadBalancerStats();

  private ServiceCombLoadBalancerStats() {
    Timer timer = new Timer("LoadBalancerStatsTimer", true);
    timer.schedule(new TimerTask() {
      private MicroserviceInstancePing ping = SPIServiceUtils.getPriorityHighestService(MicroserviceInstancePing.class);

      @Override
      public void run() {
        try {
          Map<ServiceCombServer, ServiceCombServerStats> allServers = SERVER_STATES_CACHE.asMap();
          Iterator<ServiceCombServer> instances = allServers.keySet().iterator();
          while (instances.hasNext()) {
            ServiceCombServer server = instances.next();
            // will not cause reload
            ServiceCombServerStats stats = allServers.get(server);
            if ((System.currentTimeMillis() - stats.getLastVisitTime() < TIMER_INTERVAL_MILLIS) && !ping
                .ping(server.getInstance())) {
              LOGGER.info("ping mark server {} failure.", server.getInstance().getInstanceId());
              markFailure(server);
            }
          }
        } catch (Throwable e) {
          LOGGER.warn("LoadBalancerStatsTimer error.", e);
        }
      }
    }, TIMER_INTERVAL_MILLIS, TIMER_INTERVAL_MILLIS);
  }

  public void markIsolated(ServiceCombServer server, boolean isolated) {
    try {
      SERVER_STATES_CACHE.get(server).markIsolated(isolated);
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
    }
  }

  public void markSuccess(ServiceCombServer server) {
    try {
      SERVER_STATES_CACHE.get(server).markSuccess();
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
    }
  }

  public void markFailure(ServiceCombServer server) {
    try {
      SERVER_STATES_CACHE.get(server).markFailure();
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
    }
  }

  public ServiceCombServerStats getServiceCombServerStats(ServiceCombServer server) {
    try {
      return SERVER_STATES_CACHE.get(server);
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
      return null;
    }
  }

  public ServiceCombServer getServiceCombServer(MicroserviceInstance instance) {
    for (ServiceCombServer server : SERVER_STATES_CACHE.asMap().keySet()) {
      if (server.getInstance().equals(instance)) {
        return server;
      }
    }
    return null;
  }
}

