package com.ctrip.apollo.internals;

import com.ctrip.apollo.Config;

import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class SimpleConfig implements Config {
  private String m_namespace;
  private ConfigRepository m_configRepository;
  private Properties m_configProperties;

  public SimpleConfig(String namespace, ConfigRepository configRepository) {
    m_namespace = namespace;
    m_configRepository = configRepository;
    this.initialize();
  }

  private void initialize() {
    try {
      m_configProperties = m_configRepository.loadConfig();
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Init Apollo Remote Config failed - namespace: %s",
              m_namespace), ex);
    }
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    return this.m_configProperties.getProperty(key, defaultValue);
  }

}
