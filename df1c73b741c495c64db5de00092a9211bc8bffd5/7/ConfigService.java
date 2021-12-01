package com.ctrip.apollo.biz.service;

import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.core.dto.ApolloConfig;

/**
 * Config Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigService {
  /**
   * Load configuration from database
   */
  ApolloConfig loadConfig(String appId, String clusterName, String versionName);

  /**
   * Load Version by appId and versionName from database
   */
  Version loadVersionByAppIdAndVersionName(String appId, String versionName);

  /**
   * Load Config by version and clusterName from database
   */
  ApolloConfig loadConfigByVersionAndClusterName(Version version, String clusterName);
}
