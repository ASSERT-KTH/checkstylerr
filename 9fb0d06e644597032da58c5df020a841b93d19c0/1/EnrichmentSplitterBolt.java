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
package org.apache.metron.enrichment.bolt;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.metron.common.Constants;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.common.configuration.enrichment.handler.ConfigHandler;
import org.apache.metron.common.utils.MessageUtils;
import org.apache.metron.enrichment.configuration.Enrichment;
import org.apache.metron.enrichment.utils.EnrichmentUtils;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnrichmentSplitterBolt extends SplitBolt<JSONObject> {

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<Enrichment> enrichments;
  protected String messageFieldName;
  private transient JSONParser parser;


  public EnrichmentSplitterBolt(String zookeeperUrl) {
    super(zookeeperUrl);
  }

  public EnrichmentSplitterBolt withEnrichments(List<Enrichment> enrichments) {
    this.enrichments = enrichments;
    return this;
  }

  public EnrichmentSplitterBolt withMessageFieldName(String messageFieldName) {
    this.messageFieldName = messageFieldName;
    return this;
  }
  @Override
  public void prepare(Map map, TopologyContext topologyContext) {
    parser = new JSONParser();
  }
  @Override
  public String getKey(Tuple tuple, JSONObject message) {
    String key = null, guid = null;
    try {
      key = tuple.getStringByField("key");
      guid = (String)message.get(Constants.GUID);
    }
    catch(Throwable t) {
      //swallowing this just in case.
    }
    if(key != null) {
      return key;
    }
    else if(guid != null) {
      return guid;
    }
    else {
      return UUID.randomUUID().toString();
    }
  }

  @Override
  public JSONObject generateMessage(Tuple tuple) {
    JSONObject message = null;
    if (messageFieldName == null) {
      byte[] data = tuple.getBinary(0);
      try {
        message = (JSONObject) parser.parse(new String(data, "UTF8"));
        message.put(getClass().getSimpleName().toLowerCase() + ".splitter.begin.ts", "" + System.currentTimeMillis());
      } catch (ParseException | UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    } else {
      message = (JSONObject) tuple.getValueByField(messageFieldName);
      message.put(getClass().getSimpleName().toLowerCase() + ".splitter.begin.ts", "" + System.currentTimeMillis());
    }
    return message;
  }

  @Override
  public Set<String> getStreamIds() {
    Set<String> streamIds = new HashSet<>();
    for(Enrichment enrichment: enrichments) {
      streamIds.add(enrichment.getType());
    }
    return streamIds;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, List<JSONObject>> splitMessage(JSONObject message) {
    Map<String, List<JSONObject>> streamMessageMap = new HashMap<>();
    String sensorType = MessageUtils.getSensorType(message);
    Map<String, Object> enrichmentFieldMap = getFieldMap(sensorType);
    Map<String, ConfigHandler> fieldToHandler = getFieldToHandlerMap(sensorType);
    Set<String> enrichmentTypes = new HashSet<>(enrichmentFieldMap.keySet());
    enrichmentTypes.addAll(fieldToHandler.keySet());
    for (String enrichmentType : enrichmentTypes) {
      Object fields = enrichmentFieldMap.get(enrichmentType);
      ConfigHandler retriever = fieldToHandler.get(enrichmentType);

      List<JSONObject> enrichmentObject = retriever.getType()
              .splitByFields( message
                      , fields
                      , field -> getKeyName(enrichmentType, field)
                      , retriever
              );
      for(JSONObject eo : enrichmentObject) {
        eo.put(Constants.SENSOR_TYPE, sensorType);
      }
      streamMessageMap.put(enrichmentType, enrichmentObject);
    }
    message.put(getClass().getSimpleName().toLowerCase() + ".splitter.end.ts", "" + System.currentTimeMillis());
    return streamMessageMap;
  }

  protected Map<String, ConfigHandler> getFieldToHandlerMap(String sensorType) {
    if(sensorType != null) {
      SensorEnrichmentConfig config = getConfigurations().getSensorEnrichmentConfig(sensorType);
      if (config != null) {
        return config.getEnrichment().getEnrichmentConfigs();
      } else {
        LOG.debug("Unable to retrieve a sensor enrichment config of {}", sensorType);
      }
    } else {
      LOG.error("Trying to retrieve a field map with sensor type of null");
    }
    return new HashMap<>();
  }
  protected Map<String, Object > getFieldMap(String sensorType) {
    if(sensorType != null) {
      SensorEnrichmentConfig config = getConfigurations().getSensorEnrichmentConfig(sensorType);
      if (config != null) {
        return config.getEnrichment().getFieldMap();
      } else {
        LOG.debug("Unable to retrieve a sensor enrichment config of {}", sensorType);
      }
    } else {
      LOG.error("Trying to retrieve a field map with sensor type of null");
    }
    return new HashMap<>();
  }

  protected String getKeyName(String type, String field) {
    return EnrichmentUtils.getEnrichmentKey(type, field);
  }

  @Override
  public void declareOther(OutputFieldsDeclarer declarer) {

  }

  @Override
  public void emitOther(Tuple tuple, JSONObject message) {

  }
}
