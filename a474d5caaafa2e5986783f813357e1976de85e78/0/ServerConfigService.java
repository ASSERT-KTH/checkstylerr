package com.ctrip.framework.apollo.biz.service;

import com.google.common.base.Strings;

import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import com.ctrip.framework.apollo.biz.repository.ServerConfigRepository;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.foundation.Foundation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ServerConfigService {
  @Autowired
  private ServerConfigRepository serverConfigRepository;

  public String getValue(String key) {
    ServerConfig serverConfig = null;

    //1. Load from cluster config
    if (!Strings.isNullOrEmpty(System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY))) {
      serverConfig =
          serverConfigRepository.findTopByKeyAndCluster(key, System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY));
    }

    //2. Fall back to data center config
    if (serverConfig == null && !Strings.isNullOrEmpty(getDataCenter())) {
      serverConfig = serverConfigRepository.findTopByKeyAndCluster(key, getDataCenter());
    }

    //3. Fall back to default cluster config
    if (serverConfig == null) {
      serverConfig =
          serverConfigRepository.findTopByKeyAndCluster(key, ConfigConsts.CLUSTER_NAME_DEFAULT);
    }

    return serverConfig == null ? null : serverConfig.getValue();
  }

  public String getValue(String key, String defaultValue) {
    String value = getValue(key);
    return value == null ? defaultValue : value;
  }

  String getDataCenter() {
    return Foundation.server().getDataCenter();
  }

}
