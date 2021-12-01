package com.ctrip.apollo.client;

import com.google.common.collect.Lists;

import com.ctrip.apollo.client.config.Config;
import com.ctrip.apollo.client.manager.ConfigManager;
import com.ctrip.apollo.client.manager.ConfigManagerManager;
import com.ctrip.apollo.core.ConfigConsts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry point for client config use
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigService {
  private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
  private static ConfigService instance = new ConfigService();
  private List<ConfigManager> configManagers;

  private ConfigService() {
    this.loadConfigManagers();
  }

  /**
   * Get the config instance with default namespace
   * @return config instance
   * @throws RuntimeException if config could not be loaded for the default namespace
   */
  public static Config getConfig() {
    return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * Get the config instance for the namespace
   * @param namespace the namespace of the config
   * @return config instance
   * @throws RuntimeException if config could not be loaded for the specified namespace
   */
  public static Config getConfig(String namespace) {
    return instance.doGetConfig(namespace);
  }

  Config doGetConfig(String namespace) {
    for (ConfigManager configManager : this.configManagers) {
      try {
        return configManager.findOrCreate(namespace);
      } catch (Throwable th) {
        logger.error("Get config failed for namespace {} using config manager {}", namespace,
            configManager.getClass(), th);
      }
    }
    throw new RuntimeException(String.format("Could not get config for namespace: %s", namespace));
  }

  void loadConfigManagers() {
    configManagers = Lists.newArrayList();
    ServiceLoader<ConfigManagerManager> configManagerManagers =
        ServiceLoader.load(ConfigManagerManager.class);
    for (ConfigManagerManager configManagerManager : configManagerManagers) {
      configManagers.add(configManagerManager.getConfigManager());
    }
  }
}
