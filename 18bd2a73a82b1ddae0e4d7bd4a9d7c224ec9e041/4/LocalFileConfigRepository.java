package com.ctrip.apollo.internals;

import com.google.common.base.Preconditions;

import com.ctrip.apollo.util.ConfigUtil;
import com.dianping.cat.Cat;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalFileConfigRepository implements ConfigRepository {
  private static final Logger logger = LoggerFactory.getLogger(LocalFileConfigRepository.class);
  private PlexusContainer m_container;
  private final String m_namespace;
  private final File m_baseDir;
  private final ConfigUtil m_configUtil;
  private Properties m_fileProperties;
  private ConfigRepository m_fallback;

  public LocalFileConfigRepository(File baseDir, String namespace) {
    m_baseDir = baseDir;
    m_namespace = namespace;
    m_container = ContainerLoader.getDefaultContainer();
    try {
      m_configUtil = m_container.lookup(ConfigUtil.class);
    } catch (ComponentLookupException e) {
      throw new IllegalStateException("Unable to load component!", e);
    }
  }

  @Override
  public Properties loadConfig() {
    if (m_fileProperties == null) {
      initLocalConfig();
    }
    Properties result = new Properties();
    result.putAll(m_fileProperties);
    return result;
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    m_fallback = fallbackConfigRepository;
  }

  void initLocalConfig() {
    if (m_fallback != null) {
      try {
        m_fileProperties = m_fallback.loadConfig();
        //TODO register change listener
        persistLocalCacheFile(m_baseDir, m_namespace);
        return;
      } catch (Throwable ex) {
        logger.error("Load config from fallback loader failed", ex);
        Cat.logError(ex);
      }
    }

    try {
      m_fileProperties = this.loadFromLocalCacheFile(m_baseDir, m_namespace);
    } catch (IOException ex) {
      throw new RuntimeException("Loading config from local cache file failed", ex);
    }
  }

  private Properties loadFromLocalCacheFile(File baseDir, String namespace) throws IOException {
    Preconditions.checkNotNull(baseDir, "Basedir cannot be null");

    File file = assembleLocalCacheFile(baseDir, namespace);
    Properties properties = null;

    if (file.isFile() && file.canRead()) {
      InputStream in = null;

      try {
        in = new FileInputStream(file);

        properties = new Properties();
        properties.load(in);
      } catch (IOException e) {
        logger.error("Loading config from local cache file {} failed", file.getAbsolutePath(), e);
        Cat.logError(e);
        throw e;
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException e) {
          // ignore
        }
      }
    } else {
      String message =
          String.format("Cannot read from local cache file %s", file.getAbsolutePath());
      logger.error(message);
      throw new RuntimeException(message);
    }

    return properties;
  }

  void persistLocalCacheFile(File baseDir, String namespace) {
    if (baseDir == null) {
      return;
    }
    File file = assembleLocalCacheFile(baseDir, namespace);

    OutputStream out = null;

    try {
      out = new FileOutputStream(file);
      m_fileProperties.store(out, "Persisted by DefaultConfig");
    } catch (IOException ex) {
      logger.error("Persist local cache file {} failed", file.getAbsolutePath(), ex);
      Cat.logError(ex);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }

  File assembleLocalCacheFile(File baseDir, String namespace) {
    String fileName = String.format("%s-%s-%s.properties", m_configUtil.getAppId(),
        m_configUtil.getCluster(), namespace);
    return new File(baseDir, fileName);
  }
}
