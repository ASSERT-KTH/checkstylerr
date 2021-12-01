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

package org.apache.servicecomb.config.kie.client;

import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.servicecomb.config.kie.model.KVDoc;
import org.apache.servicecomb.config.kie.model.KVResponse;
import org.apache.servicecomb.config.kie.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

public class KieUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieUtil.class);

  private static final String LABEL_ENV = "environment";

  private static final String LABEL_APP = "app";

  private static final String LABEL_SERVICE = "service";

  private static final String LABEL_VERSION = "version";

  private static final String STATUS_ENABLED = "enabled";

  public static String encrypt(String dataStr) {
    MessageDigest messageDigest = null;
    String result = "";
    try {
      messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(dataStr.getBytes(StandardCharsets.UTF_8));
      result = new BigInteger(1, messageDigest.digest(dataStr.getBytes(StandardCharsets.UTF_8)))
          .toString(16);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Failed to generate MD5 . ", e);
    }
    return result;
  }

  public static Map<String, Object> getConfigByLabel(KVResponse resp) {
    Map<String, Object> resultMap = new HashMap<>();
    List<KVDoc> appList = new ArrayList<>();
    List<KVDoc> serviceList = new ArrayList<>();
    List<KVDoc> versionList = new ArrayList<>();
    for (KVDoc kvDoc : resp.getData()) {
      if (!StringUtils.isEmpty(kvDoc.getStatus()) && !kvDoc.getStatus().equals(STATUS_ENABLED)) {
        continue;
      }
      Map<String, String> labelsMap = kvDoc.getLabels();
      if (labelsMap.containsKey(LABEL_APP) && labelsMap.get(LABEL_APP)
          .equals(KieConfig.INSTANCE.getAppName())
          && labelsMap.containsKey(LABEL_ENV) && labelsMap.get(LABEL_ENV)
          .equals(KieConfig.INSTANCE.getEnvironment())) {
        if (!labelsMap.containsKey(LABEL_SERVICE)) {
          appList.add(kvDoc);
        }
        if (labelsMap.containsKey(LABEL_SERVICE) && labelsMap.get(LABEL_SERVICE)
            .equals(KieConfig.INSTANCE.getServiceName())) {
          if (!kvDoc.getLabels().containsKey(LABEL_VERSION)) {
            serviceList.add(kvDoc);
          }
          if (labelsMap.containsKey(LABEL_VERSION) && labelsMap.get(LABEL_VERSION)
              .equals(KieConfig.INSTANCE.getServiceName())) {
            versionList.add(kvDoc);
          }
        }
      }
    }
    //kv is priority
    for (KVDoc kvDoc : appList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : serviceList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : versionList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    return resultMap;
  }

  public static Map<String, String> processValueType(KVDoc kvDoc) {
    ValueType vtype;
    try {
      vtype = ValueType.valueOf(kvDoc.getValueType());
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("value type not support");
    }
    Properties properties = new Properties();
    Map<String, String> kvMap = new HashMap<>();
    try {
      if (vtype == (ValueType.YMAL) || vtype == (ValueType.YML)) {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ByteArrayResource(kvDoc.getValue().getBytes()));
        properties = yamlFactory.getObject();
      } else if (vtype == (ValueType.PROPERTIES)) {
        properties.load(new StringReader(kvDoc.getValue()));
      } else if (vtype == (ValueType.TEXT) || vtype == (ValueType.STRING)) {
        kvMap.put(kvDoc.getKey(), kvDoc.getValue());
        return kvMap;
      } else {
        kvMap.put(kvDoc.getKey(), kvDoc.getValue());
        return kvMap;
      }
      kvMap = toMap(kvDoc.getKey(), properties);
      return kvMap;
    } catch (Exception e) {
      LOGGER.error("read config failed");
    }
    return Collections.emptyMap();
  }


  @SuppressWarnings("unchecked")
  private static Map<String, String> toMap(String prefix, Properties properties) {
    Map<String, String> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      if (!StringUtils.isEmpty(prefix)) {
        key = prefix + "." + key;
      }
      Object value = properties.getProperty(key);
      if (value != null) {
        result.put(key, ((String) value).trim());
      } else {
        result.put(key, null);
      }
    }
    return result;
  }
}
