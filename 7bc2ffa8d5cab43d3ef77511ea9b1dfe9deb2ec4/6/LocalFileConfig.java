package com.ctrip.apollo.client.config.impl;

import com.ctrip.apollo.client.config.Config;
import com.ctrip.apollo.client.loader.ConfigLoader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalFileConfig implements Config {
  private ConfigLoader configLoader;
  private String namespace;

  public LocalFileConfig(ConfigLoader configLoader, String namespace) {
    this.configLoader = configLoader;
    this.namespace = namespace;
  }

  @Override
  public String getProperty(String key) {
    return null;
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    return value == null ? defaultValue : value;
  }
}
