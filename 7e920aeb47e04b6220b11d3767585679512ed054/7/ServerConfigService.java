package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

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
    ServerConfig serverConfig = serverConfigRepository.findByKey(key);

    return serverConfig == null ? null : serverConfig.getValue();
  }

}
