package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ServerConfigService {
  @Autowired
  private ServerConfigRepository serverConfigRepository;

  @Autowired
  private Environment environment;

  public String getValue(String key) {
    if (environment.containsProperty(key)) {
      return environment.getProperty(key);
    }

    ServerConfig serverConfig = serverConfigRepository.findByKey(key);

    return serverConfig == null ? null : serverConfig.getValue();
  }

}
