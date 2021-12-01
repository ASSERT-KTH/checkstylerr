package com.ctrip.apollo.biz.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
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
  private ClusterRepository clusterRepository;

  @Autowired
  private VersionRepository versionRepository;

  @Autowired
  private ReleaseRepository releaseRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private TypeReference<Map<String, Object>> configurationTypeReference =
      new TypeReference<Map<String, Object>>() {};

  public Release findRelease(String appId, String clusterName, String groupName, String versionName) {
    Cluster cluster = clusterRepository.findByAppIdAndName(appId, clusterName);
    if (cluster == null) {
      return null;
    }
    Version version = versionRepository.findByClusterIdAndName(cluster.getId(), versionName);
    if (version == null) {
      return null;
    }
    Release release = releaseRepository.findByGroupNameAndVersionId(groupName, version.getId());
    return release;
  }

  /**
   * Load configuration from database
   */
  public ApolloConfig loadConfig(Release release, String groupName, String versionName) {
    if(release==null){
      return null;
    }
    ApolloConfig config =
        new ApolloConfig(release.getAppId(), release.getClusterName(), groupName, versionName, release.getId());
    config.setConfigurations(transformConfigurationToMap(release.getConfigurations()));
    return config;
  }

  Map<String, Object> transformConfigurationToMap(String configurations) {
    try {
      return objectMapper.readValue(configurations, configurationTypeReference);
    } catch (IOException e) {
      e.printStackTrace();
      return Maps.newHashMap();
    }
  }
}
