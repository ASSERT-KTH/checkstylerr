package com.ctrip.apollo.biz.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * Config Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConfigService {

  @Autowired
  private ReleaseRepository releaseRepository;

  private ObjectMapper objectMapper = new ObjectMapper();

  private TypeReference<Map<String, String>> configurationTypeReference =
      new TypeReference<Map<String, String>>() {};

  public Release findRelease(String appId, String clusterName, String namespaceName) {
    Release release = releaseRepository.findLatest(appId, clusterName, namespaceName);
    return release;
  }

  /**
   * Load configuration from database
   */
  public ApolloConfig loadConfig(Release release, String namespaceName) {
    if (release == null) {
      return null;
    }
    ApolloConfig config = new ApolloConfig(release.getAppId(), release.getClusterName(),
        namespaceName, release.getId());
    config.setConfigurations(transformConfigurationToMap(release.getConfigurations()));
    return config;
  }

  Map<String, String> transformConfigurationToMap(String configurations) {
    try {
      return objectMapper.readValue(configurations, configurationTypeReference);
    } catch (IOException e) {
      e.printStackTrace();
      return Maps.newHashMap();
    }
  }
}
