package com.ctrip.apollo.internals;

import com.ctrip.apollo.Config;
import com.dianping.cat.Cat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultConfig implements Config {
  private final String m_namespace;

  private Properties m_resourceProperties;

  private Properties m_fileProperties;

  public DefaultConfig(File baseDir, String namespace) {
    m_namespace = namespace;
    m_resourceProperties = loadFromResource(namespace);
    m_fileProperties = loadFromFile(baseDir, namespace);
  }

  private Properties loadFromResource(String namespace) {
    String name = String.format("/META-INF/config/%s.properties", namespace);
    InputStream in = getClass().getResourceAsStream(name);
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

  @Override
  public String getProperty(String key, String defaultValue) {
    // step 1: check system properties, i.e. -Dkey=value
    String value = System.getProperty(key);

    // step 2: check local cached properties file
    if (value == null) {
      if (m_fileProperties != null) {
        value = (String) m_fileProperties.get(key);
      }
    }

    // step 3: check env variable, i.e. PATH=...
    if (value == null) {
      value = System.getenv(key); // TODO fix naming issues
    }

    // step 4: check properties file from classpath
    if (value == null) {
      if (m_resourceProperties != null) {
        value = (String) m_resourceProperties.get(key);
      }
    }

    return value;
  }
}
