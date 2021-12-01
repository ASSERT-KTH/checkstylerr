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
package org.apache.servicecomb.zeroconfig.client;

import com.google.common.annotations.VisibleForTesting;
import io.vertx.core.json.Json;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.apache.servicecomb.registry.version.Version;
import org.apache.servicecomb.registry.version.VersionRule;
import org.apache.servicecomb.registry.version.VersionRuleUtils;
import org.apache.servicecomb.registry.version.VersionUtils;
import org.apache.servicecomb.zeroconfig.server.ServerMicroserviceInstance;
import org.apache.servicecomb.zeroconfig.server.ServerUtil;
import org.apache.servicecomb.zeroconfig.server.ZeroConfigRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.servicecomb.zeroconfig.ZeroConfigRegistryConstants.*;

public class ZeroConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZeroConfigClient.class);

  public static ZeroConfigClient INSTANCE = buildZeroConfigClient();

  // Constructor Parameters
  private ZeroConfigRegistryService zeroConfigRegistryService;
  private MulticastSocket multicastSocket;

  // registration objects
  private Microservice selfMicroservice;
  private MicroserviceInstance selfMicroserviceInstance;

  // Constructor
  private ZeroConfigClient(ZeroConfigRegistryService zeroConfigRegistryService,
      MulticastSocket multicastSocket) {
    this.zeroConfigRegistryService = zeroConfigRegistryService;
    this.multicastSocket = multicastSocket;
  }

  @VisibleForTesting
  public ZeroConfigClient initZeroConfigClientWithMocked(
      ZeroConfigRegistryService zeroConfigRegistryService,
      MulticastSocket multicastSocket) {
    this.zeroConfigRegistryService = zeroConfigRegistryService;
    this.multicastSocket = multicastSocket;
    return this;
  }

  public void init() {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(
        loader.getConfigModels());
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    selfMicroservice = microserviceFactory.create(microserviceDefinition);
    selfMicroserviceInstance = selfMicroservice.getInstance();

    // set serviceId
    if (StringUtils.isEmpty(selfMicroservice.getServiceId())) {
      String serviceId = ClientUtil.generateServiceId(selfMicroservice);
      selfMicroservice.setServiceId(serviceId);
      selfMicroserviceInstance.setServiceId(serviceId);
    }

    // set instanceId
    if (StringUtils.isEmpty(selfMicroserviceInstance.getInstanceId())) {
      String instanceId = ClientUtil.generateServiceInstanceId();
      selfMicroserviceInstance.setInstanceId(instanceId);
    }

  }

  // builder method
  private static ZeroConfigClient buildZeroConfigClient() {
    MulticastSocket multicastSocket = null;
    try {
      multicastSocket = new MulticastSocket();
      multicastSocket.setLoopbackMode(false);
    } catch (IOException e) {
      // since we support multiple registries, not blocking other type of registries
      LOGGER.error("Failed to create MulticastSocket object in Zero-Config mode", e);
      //throw new ServiceCombException("Failed to create MulticastSocket object", e);
    }
    return new ZeroConfigClient(new ZeroConfigRegistryService(), multicastSocket);
  }

  public boolean register() {
    String serviceInstanceId = doRegister(ClientUtil.convertToRegisterDataModel(selfMicroserviceInstance, selfMicroservice));
    return StringUtils.isNotEmpty(serviceInstanceId);
  }

  private String doRegister(ServerMicroserviceInstance instance) {
    try {
      byte[] instanceData = Json.encode(instance).getBytes(ENCODE);
      DatagramPacket instanceDataPacket = new DatagramPacket(instanceData, instanceData.length,
          InetAddress.getByName(GROUP), PORT);
      this.multicastSocket.send(instanceDataPacket);

      // set this variable for heartbeat itself status
      instance.setEvent(HEARTBEAT_EVENT);
      ClientUtil.INSTANCE.setServiceInstanceForHeartbeat(instance);
    } catch (IOException e) {
      LOGGER.error(
          "Failed to Multicast Microservice Instance Registration Event in Zero-Config mode. servcieId: {} instanceId:{}",
          instance.getServiceId(), instance.getInstanceId(), e);
      return null;
    }
    return instance.getInstanceId();
  }

  public boolean unregister() {
    ServerMicroserviceInstance foundInstance = preUnregisterCheck();
    if (foundInstance == null) {
      LOGGER.warn(
          "Failed to unregister as Microservice Instance doesn't exist in server side in Zero-Config mode");
      return false;
    }

    try {
      LOGGER.info(
          "Start Multicast Microservice Instance Unregister Event in Zero-Config mode. Service ID: {}, Instance ID:{}",
          foundInstance.getServiceId(), foundInstance.getInstanceId());
      Map<String, String> unregisterEventMap = new HashMap<>();
      unregisterEventMap.put(EVENT, UNREGISTER_EVENT);
      unregisterEventMap.put(SERVICE_ID, foundInstance.getServiceId());
      unregisterEventMap.put(INSTANCE_ID, foundInstance.getInstanceId());
      byte[] unregisterEventBytes = unregisterEventMap.toString().getBytes(ENCODE);
      DatagramPacket unregisterEventDataPacket = new DatagramPacket(unregisterEventBytes,
          unregisterEventBytes.length, InetAddress.getByName(GROUP), PORT);
      this.multicastSocket.send(unregisterEventDataPacket);
      return true;
    } catch (IOException e) {
      LOGGER.error(
          "Failed to Multicast Microservice Instance Unregister Event in Zero-Config mode. Service ID: {}, Instance ID:{}",
          foundInstance.getServiceId(), foundInstance.getInstanceId(), e);
      return false;
    }

  }

  public List<Microservice> getAllMicroservices() {
    List<Microservice> resultAllServices = new ArrayList<>();
    Map<String, Map<String, ServerMicroserviceInstance>> allServicesMap = ServerUtil.microserviceInstanceMap;
    allServicesMap.forEach((serviceId, instanceIdMap) -> {
      instanceIdMap.forEach((instanceId, instance) -> {
        resultAllServices.add(ClientUtil.convertToClientMicroservice(instance));
      });
    });
    return resultAllServices;
  }

  public Microservice getMicroservice(String microserviceId) {

    // for registration
    if (selfMicroservice.getServiceId().equals(microserviceId)) {
      return selfMicroservice;
    } else {
      // called when consumer discover provider for the very first time
      return ClientUtil
          .convertToClientMicroservice(zeroConfigRegistryService.getMicroservice(microserviceId));
    }
  }

  public String getSchema(String microserviceId, String schemaId) {
    LOGGER.info("Retrieve schema content for Microservice ID: {}, Schema ID: {}",
        microserviceId, schemaId);
    // called by service registration task when registering itself
    if (selfMicroservice.getServiceId().equals(microserviceId)) {
      return selfMicroservice.getSchemaMap().computeIfPresent(schemaId, (k, v) -> v);
    } else {
      // TODO will need to rely on the registry-schema-discovery module to getSchema
      return null;
    }
  }

  public MicroserviceInstance findMicroserviceInstance(String serviceId, String instanceId) {
    ServerMicroserviceInstance instance = this.zeroConfigRegistryService
        .findServiceInstance(serviceId, instanceId);

    if (instance == null) {
      LOGGER.error(
          "Invalid serviceId OR instanceId! Failed to retrieve Microservice Instance for serviceId {} and instanceId {}",
          serviceId, instanceId);
      return null;
    }
    return ClientUtil.convertToClientMicroserviceInstance(instance);
  }

  public MicroserviceInstances findServiceInstances(String appId, String providerServiceName,
      String strVersionRule) {
    LOGGER.info(
        "Find service instance for App ID: {}, Provider ServiceName: {}, versionRule: {} in Zero-Config mode",
        appId, providerServiceName, strVersionRule);

    MicroserviceInstances resultMicroserviceInstances = new MicroserviceInstances();
    FindInstancesResponse response = new FindInstancesResponse();
    List<MicroserviceInstance> resultInstanceList = new ArrayList<>();

    // 1.  find matched appId and serviceName from "Server"
    List<ServerMicroserviceInstance> tempServerInstanceList = this.zeroConfigRegistryService.
        findServiceInstances(appId, providerServiceName);

    // 2.  find matched instance based on the strVersionRule
    VersionRule versionRule = VersionRuleUtils.getOrCreate(strVersionRule);

    ServerMicroserviceInstance latestVersionInstance = findLatestVersionInstance(
        tempServerInstanceList, versionRule);
    if (latestVersionInstance != null) {
      Version latestVersion = VersionUtils.getOrCreate(latestVersionInstance.getVersion());
      for (ServerMicroserviceInstance serverInstance : tempServerInstanceList) {
        Version version = VersionUtils.getOrCreate(serverInstance.getVersion());
        if (!versionRule.isMatch(version, latestVersion)) {
          continue;
        }
        resultInstanceList.add(ClientUtil.convertToClientMicroserviceInstance(serverInstance));
      }
    }

    response.setInstances(resultInstanceList);
    resultMicroserviceInstances.setInstancesResponse(response);
    return resultMicroserviceInstances;
  }

  private ServerMicroserviceInstance findLatestVersionInstance(
      List<ServerMicroserviceInstance> instanceList, VersionRule versionRule) {
    Version latestVersion = null;
    ServerMicroserviceInstance latestVersionInstance = null;
    for (ServerMicroserviceInstance serverInstance : instanceList) {
      Version version = VersionUtils.getOrCreate(serverInstance.getVersion());
      if (!versionRule.isAccept(version)) {
        continue;
      }

      if (latestVersion == null || version.compareTo(latestVersion) > 0) {
        latestVersion = version;
        latestVersionInstance = serverInstance;
      }
    }
    return latestVersionInstance;
  }

  private ServerMicroserviceInstance preUnregisterCheck() {
    ServerMicroserviceInstance instance = zeroConfigRegistryService
        .findServiceInstance(selfMicroserviceInstance.getServiceId(),
            selfMicroserviceInstance.getInstanceId());
    return instance;
  }

  public Microservice getSelfMicroservice() {
    return selfMicroservice;
  }

  @VisibleForTesting
  public void setSelfMicroservice(
      Microservice selfMicroservice) {
    this.selfMicroservice = selfMicroservice;
  }

  public MicroserviceInstance getSelfMicroserviceInstance() {
    return selfMicroserviceInstance;
  }

  @VisibleForTesting
  public void setSelfMicroserviceInstance(
      MicroserviceInstance selfMicroserviceInstance) {
    this.selfMicroserviceInstance = selfMicroserviceInstance;
  }

}
