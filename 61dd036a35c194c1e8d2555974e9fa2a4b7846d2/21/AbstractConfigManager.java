package com.ctrip.apollo.client.manager.impl;

import com.ctrip.apollo.client.config.Config;
import com.ctrip.apollo.client.factory.ConfigFactory;
import com.ctrip.apollo.client.manager.ConfigManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigManager implements ConfigManager {
  private ConcurrentMap<String, Config> configs;
  private ConfigFactory configFactory;

  public AbstractConfigManager(
      ConfigFactory configFactory) {
    this.configs = new ConcurrentHashMap<>();
    this.configFactory = configFactory;
  }

  @Override
  public Config findOrCreate(String namespace) {
    Config config = configs.get(namespace);
    if (config == null) {
      synchronized (configs) {
        config = configs.get(namespace);
        if (config == null) {
          config = configFactory.createConfig(namespace);
          configs.put(namespace, config);
        }
      }
    }
    return config;
  }
}
