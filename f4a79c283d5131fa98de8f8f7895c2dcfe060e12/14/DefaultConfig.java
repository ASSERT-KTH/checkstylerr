package com.ctrip.apollo.internals;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;
import com.dianping.cat.Cat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfig implements Config, ConfigLoader {
  private final String m_namespace;
  private final File m_baseDir;
  private Properties m_resourceProperties;
  private Properties m_fileProperties;
  private ConfigLoader m_fallbackLoader;

  public DefaultConfig(File baseDir, String namespace, ConfigLoader fallbackLoader) {
    m_namespace = namespace;
    m_baseDir = baseDir;
    m_resourceProperties = loadFromResource(m_namespace);
    m_fallbackLoader = fallbackLoader;
    this.initLocalConfig();
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    // step 1: check system properties, i.e. -Dkey=value
    String value = System.getProperty(key);

    // step 2: check local cached properties file
    if (value == null) {
      value = m_fileProperties.getProperty(key);
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

  private Properties loadFromFile(File baseDir, String namespace) {
    if (baseDir == null) {
      return null;
    }

    File file = new File(baseDir, namespace + ".properties");
    Properties properties = null;

    if (file.isFile() && file.canRead()) {
      InputStream in = null;

      try {
        in = new FileInputStream(file);

        properties = new Properties();
        properties.load(in);
      } catch (IOException e) {
        Cat.logError(e);
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException e) {
          // ignore
        }
      }
    }

    return properties;
  }

  void initLocalConfig() {
    m_fileProperties = this.loadFromFile(m_baseDir, m_namespace);
    //TODO check if local file is expired
    if (m_fileProperties == null && m_fallbackLoader != null) {
      m_fileProperties = m_fallbackLoader.loadConfig();
    }
    if (m_fileProperties == null) {
      throw new RuntimeException(
          String.format("Init Apollo Local Config failed - namespace: %s",
              m_namespace));
    }
    //TODO persist file
  }

  @Override
  public Properties loadConfig() {
    Properties result = new Properties();
    result.putAll(m_fileProperties);
    return result;
  }

  public ConfigLoader getFallbackLoader() {
    return m_fallbackLoader;
  }
}
