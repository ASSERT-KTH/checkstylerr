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

package org.apache.servicecomb.serviceregistry;

import java.util.List;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;

public class DiscoveryManager {
  public static DiscoveryManager INSTANCE = new DiscoveryManager();

  private List<Discovery> discoveryList = SPIServiceUtils.getOrLoadSortedService(Discovery.class);

  private final AppManager appManager = new AppManager();

  private InstanceCacheManager instanceCacheManager = new InstanceCacheManagerNew(appManager);

  private final MicroserviceDefinition microserviceDefinition;

  public DiscoveryManager() {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
  }

  public MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule, String revision) {
    MicroserviceInstances result = new MicroserviceInstances();

    discoveryList
        .forEach(discovery -> {
          MicroserviceInstances microserviceInstances = discovery.findServiceInstances(appId, serviceName, versionRule);
          result.mergeMicroserviceInstances(microserviceInstances);
          discovery.setRevision(microserviceInstances.getRevision());
        });

    return result;
  }

  public InstanceCacheManager getInstanceCacheManager() {
    return this.instanceCacheManager;
  }

  public AppManager getAppManager() {
    return this.appManager;
  }

  public MicroserviceInstance findMicroserviceInstance(String serviceId, String instanceId) {
    for (Discovery discovery : discoveryList) {
      MicroserviceInstance microserviceInstance = discovery.findMicroserviceInstance(serviceId, instanceId);
      if (microserviceInstance != null) {
        return microserviceInstance;
      }
    }
    return null;
  }

  public String getSchema(String microserviceId, String schemaId) {
    for (Discovery discovery : discoveryList) {
      String schema = discovery.getSchema(microserviceId, schemaId);
      if (schema != null) {
        return schema;
      }
    }
    return null;
  }

  public Microservice getMicroservice(String microserviceId) {
    for (Discovery discovery : discoveryList) {
      Microservice microservice = discovery.getMicroservice(microserviceId);
      if (microservice != null) {
        return microservice;
      }
    }
    return null;
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String appId, String microserviceName) {
    return appManager.getOrCreateMicroserviceVersions(appId, microserviceName);
  }

  public String getApplicationId() {
    return microserviceDefinition.getApplicationId();
  }

  public void destroy() {
    discoveryList.forEach(discovery -> discovery.destroy());
  }

  public void run() {
    discoveryList.forEach(discovery -> discovery.run());
  }
}
