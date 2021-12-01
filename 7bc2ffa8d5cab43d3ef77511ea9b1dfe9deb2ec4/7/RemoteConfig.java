package com.ctrip.apollo.client.config.impl;

import com.ctrip.apollo.client.config.Config;
import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.client.model.PropertyChange;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.dto.ApolloConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfig implements Config {
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfig.class);
  private ConfigLoader configLoader;
  private ConfigUtil configUtil;
  private String namespace;

  private ApolloConfig currentApolloConfig;
  private ApolloConfig previousApolloConfig;

  public RemoteConfig(ConfigLoader configLoader, String namespace) {
    this.configLoader = configLoader;
    this.namespace = namespace;
    this.configUtil = ConfigUtil.getInstance();
    this.initialize();
  }

  void initialize() {
    this.loadApolloConfig(
        new ApolloRegistry(this.configUtil.getAppId(), this.configUtil.getCluster(),
            namespace));
  }

  @Override
  public String getProperty(String key) {
    String value = this.currentApolloConfig.getProperty(key);
    if (value == null) {
      return null;
    }
    return value;
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    return value == null ? defaultValue : value;
  }

  synchronized ApolloConfig loadApolloConfig(ApolloRegistry apolloRegistry) {
    resetApolloRegistryConfigCache();
    ApolloConfig result =
        configLoader.loadApolloConfig(apolloRegistry, getPreviousApolloConfig());
    if (result == null) {
      logger.error("Loaded config null...");
      return null;
    }
    logger.info("Loaded config: {}", result);
    updateCurrentApolloConfigCache(result);

    return result;
  }

  void resetApolloRegistryConfigCache() {
    this.previousApolloConfig = currentApolloConfig;
    this.currentApolloConfig = null;
  }

  ApolloConfig getPreviousApolloConfig() {
    return this.previousApolloConfig;
  }

  void updateCurrentApolloConfigCache(ApolloConfig apolloConfig) {
    this.currentApolloConfig = apolloConfig;
  }

}
