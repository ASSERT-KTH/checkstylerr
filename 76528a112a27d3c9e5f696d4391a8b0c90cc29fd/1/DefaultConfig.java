package com.ctrip.apollo.internals;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;
import com.dianping.cat.Cat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfig implements Config {
  private final String m_namespace;
  private Properties m_resourceProperties;
  private Properties m_configProperties;
  private ConfigRepository m_configRepository;

  public DefaultConfig(String namespace, ConfigRepository configRepository) {
    m_namespace = namespace;
    m_resourceProperties = loadFromResource(m_namespace);
    m_configRepository = configRepository;
    initialize();
  }

  private void initialize() {
    try {
      m_configProperties = m_configRepository.loadConfig();
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Init Apollo Local Config failed - namespace: %s",
              m_namespace), ex);
    }
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    // step 1: check system properties, i.e. -Dkey=value
    String value = System.getProperty(key);

    // step 2: check local cached properties file
    if (value == null) {
      value = m_configProperties.getProperty(key);
    }

    /**
     * step 3: check env variable, i.e. PATH=...
     * normally system environment variables are in UPPERCASE, however there might be exceptions.
     * so the caller should provide the key in the right case
     */
    if (value == null) {
      value = System.getenv(key);
    }

    // step 4: check properties file from classpath
    if (value == null) {
      if (m_resourceProperties != null) {
        value = (String) m_resourceProperties.get(key);
      }
    }

    return value == null ? defaultValue : value;
  }

  private Properties loadFromResource(String namespace) {
    String name = String.format("META-INF/config/%s.properties", namespace);
    InputStream in = ClassLoaderUtil.getLoader().getResourceAsStream(name);
    Properties properties = null;

    if (in != null) {
      properties = new Properties();

      try {
        properties.load(in);
      } catch (IOException e) {
        Cat.logError(e);
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    return properties;
  }


}
