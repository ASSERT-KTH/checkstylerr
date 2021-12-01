/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.rest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.metron.common.aggregator.Aggregators;
import org.apache.metron.common.configuration.ConfigurationType;
import org.apache.metron.common.configuration.ConfigurationsUtils;
import org.apache.metron.common.configuration.EnrichmentConfigurations;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.common.zookeeper.ConfigurationsCache;
import org.apache.metron.hbase.client.HBaseClient;
import org.apache.metron.rest.RestException;
import org.apache.metron.rest.service.SensorEnrichmentConfigService;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SensorEnrichmentConfigServiceImpl implements SensorEnrichmentConfigService {

    private ObjectMapper objectMapper;

    private CuratorFramework client;

    private ConfigurationsCache cache;

    private HBaseClient hBaseClient;

    @Autowired
    public SensorEnrichmentConfigServiceImpl(final ObjectMapper objectMapper,
        final CuratorFramework client, final ConfigurationsCache cache,
        final HBaseClient hBaseClient) {
      this.objectMapper = objectMapper;
      this.client = client;
      this.cache = cache;
      this.hBaseClient = hBaseClient;
    }

    @Override
    public SensorEnrichmentConfig save(String name, SensorEnrichmentConfig sensorEnrichmentConfig) throws RestException {
      try {
        ConfigurationsUtils.writeSensorEnrichmentConfigToZookeeper(name, objectMapper.writeValueAsString(sensorEnrichmentConfig).getBytes(
            StandardCharsets.UTF_8), client);
      } catch (Exception e) {
        throw new RestException(e);
      }
      return sensorEnrichmentConfig;
    }

    @Override
    public SensorEnrichmentConfig findOne(String name) throws RestException {
      EnrichmentConfigurations configs = cache.get( EnrichmentConfigurations.class);
      return configs.getSensorEnrichmentConfig(name);
    }

    @Override
    public Map<String, SensorEnrichmentConfig> getAll() throws RestException {
      Map<String, SensorEnrichmentConfig> sensorEnrichmentConfigs = new HashMap<>();
      List<String> sensorNames = getAllTypes();
      for (String name : sensorNames) {
        SensorEnrichmentConfig config = findOne(name);
        if(config != null) {
          sensorEnrichmentConfigs.put(name, config);
        }
      }
      return sensorEnrichmentConfigs;
    }

    @Override
    public List<String> getAllTypes() throws RestException {
      EnrichmentConfigurations configs = cache.get( EnrichmentConfigurations.class);
      return configs.getTypes();
    }

    @Override
    public boolean delete(String name) throws RestException {
        try {
            client.delete().forPath(ConfigurationType.ENRICHMENT.getZookeeperRoot() + "/" + name);
        } catch (KeeperException.NoNodeException e) {
            return false;
        } catch (Exception e) {
          throw new RestException(e);
        }
      return true;
    }

    /**
     * Lexicographically sorted list of available enrichments.
     */
    @Override
    public List<String> getAvailableEnrichments() throws RestException {
      try {
        List<String> enrichments = hBaseClient.readRecords();
        enrichments.sort(Comparator.naturalOrder());
        return enrichments;
      } catch (IOException e) {
        throw new RestException("Unable to retrieve enrichments", e);
      }
    }

    @Override
    public List<String> getAvailableThreatTriageAggregators() {
      return Arrays.asList(Aggregators.values()).stream().map(Enum::toString).collect(Collectors.toList());
    }
}
