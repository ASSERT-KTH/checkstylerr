package com.ctrip.apollo.biz.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.ctrip.apollo.core.dto.ApolloConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Config Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConfigService {

  @Autowired
  private ReleaseRepository releaseRepository;

  private Gson gson = new Gson();

  private Type configurationTypeReference = new TypeToken<Map<String, String>>() {}.getType();

  public Release findRelease(String appId, String clusterName, String namespaceName) {
    Release release = releaseRepository.findFirstByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(
        appId, clusterName, namespaceName);
    return release;
  }

  /**
   * Load configuration from database
   */
  public ApolloConfig loadConfig(Release release) {
    if (release == null) {
      return null;
    }
    ApolloConfig config = new ApolloConfig(release.getAppId(), release.getClusterName(),
        release.getNamespaceName(), String.valueOf(release.getId()));
    config.setConfigurations(transformConfigurationToMap(release.getConfigurations()));
    return config;
  }

  Map<String, String> transformConfigurationToMap(String configurations) {
    return gson.fromJson(configurations, configurationTypeReference);
  }
}
