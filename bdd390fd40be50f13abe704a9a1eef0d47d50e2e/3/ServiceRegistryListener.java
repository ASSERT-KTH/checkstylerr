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
package org.apache.servicecomb.core.definition;

import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_META;
import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_VERSION;
import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_VERSIONS_META;
import static org.apache.servicecomb.core.definition.CoreMetaUtils.getMicroserviceVersionsMeta;

import java.util.List;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.apache.servicecomb.foundation.common.event.SubscriberOrder;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.event.CreateMicroserviceEvent;
import org.apache.servicecomb.serviceregistry.event.CreateMicroserviceVersionEvent;
import org.apache.servicecomb.serviceregistry.event.DestroyMicroserviceEvent;

import com.google.common.eventbus.Subscribe;

import io.swagger.models.Swagger;

/**
 * subscribe event from ServiceRegistry module to create or destroy metas
 */
public class ServiceRegistryListener {
  private final SCBEngine scbEngine;

  public ServiceRegistryListener(SCBEngine scbEngine) {
    this.scbEngine = scbEngine;
    scbEngine.getEventBus().register(this);
  }

  @EnableExceptionPropagation
  @SubscriberOrder(-1000)
  @Subscribe
  public void onCreateMicroservice(CreateMicroserviceEvent event) {
    MicroserviceVersions microserviceVersions = event.getMicroserviceVersions();
    microserviceVersions.getVendorExtensions()
        .put(CORE_MICROSERVICE_VERSIONS_META,
            new ConsumerMicroserviceVersionsMeta(scbEngine, microserviceVersions));
  }

  @EnableExceptionPropagation
  @SubscriberOrder(-1000)
  @Subscribe
  public void onDestroyMicroservice(DestroyMicroserviceEvent event) {
    MicroserviceVersions microserviceVersions = event.getMicroserviceVersions();
    MicroserviceVersionsMeta microserviceVersionsMeta = microserviceVersions.getVendorExtensions()
        .get(CORE_MICROSERVICE_VERSIONS_META);
    microserviceVersionsMeta.destroy();
  }

  @EnableExceptionPropagation
  @SubscriberOrder(-1000)
  @Subscribe
  public void onCreateMicroserviceVersion(CreateMicroserviceVersionEvent event) {
    // TODO:如果失败，应该标记出错，以便删除MicroserviceVersions
    MicroserviceVersion microserviceVersion = event.getMicroserviceVersion();
    Microservice microservice = microserviceVersion.getMicroservice();

    // not shortName, to support cross app invoke
    String microserviceName = microserviceVersion.getMicroserviceName();
    List<Handler> consumerHandlerChain = scbEngine.getConsumerHandlerManager().getOrCreate(microserviceName);
    List<Handler> producerHandlerChain = scbEngine.getProducerHandlerManager().getOrCreate(microserviceName);

    MicroserviceMeta microserviceMeta = new MicroserviceMeta(scbEngine, microserviceName,
        consumerHandlerChain, producerHandlerChain);
    MicroserviceVersions microserviceVersions = microserviceVersion.getMicroserviceVersions();
    microserviceMeta.setMicroserviceVersionsMeta(getMicroserviceVersionsMeta(microserviceVersions));

    // TODO: service center do not have schema. But this logic expected to work. Deleted old code and comments.
    for (String schemaId : microservice.getSchemas()) {
      Swagger swagger = scbEngine.getSwaggerLoader().loadSwagger(microservice, schemaId);
      microserviceMeta.registerSchemaMeta(schemaId, swagger);
    }

    microserviceMeta.putExtData(CORE_MICROSERVICE_VERSION, microserviceVersion);
    microserviceVersion.getVendorExtensions().put(CORE_MICROSERVICE_META, microserviceMeta);
  }

  public void destroy() {
    scbEngine.getEventBus().unregister(this);
  }
}
