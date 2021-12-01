package com.ctrip.apollo.internals;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.ExceptionUtil;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

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
public class LocalFileConfigRepository extends AbstractConfigRepository
    implements RepositoryChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(LocalFileConfigRepository.class);
  private final PlexusContainer m_container;
  private final String m_namespace;
  private final File m_baseDir;
  private final ConfigUtil m_configUtil;
  private volatile Properties m_fileProperties;
  private volatile ConfigRepository m_fallback;

  /**
   * Constructor.
   *
   * @param baseDir   the base dir for this local file config repository
   * @param namespace the namespace
   */
  public LocalFileConfigRepository(File baseDir, String namespace) {
    m_baseDir = baseDir;
    m_namespace = namespace;
    m_container = ContainerLoader.getDefaultContainer();
    try {
      m_configUtil = m_container.lookup(ConfigUtil.class);
    } catch (ComponentLookupException ex) {
      Cat.logError(ex);
      throw new IllegalStateException("Unable to load component!", ex);
    }
    this.trySync();
  }

  @Override
  public Properties getConfig() {
    if (m_fileProperties == null) {
      sync();
    }
    Properties result = new Properties();
    result.putAll(m_fileProperties);
    return result;
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    //clear previous listener
    if (m_fallback != null) {
      m_fallback.removeChangeListener(this);
    }
    m_fallback = fallbackConfigRepository;
    trySyncFromFallback();
    fallbackConfigRepository.addChangeListener(this);
  }

  @Override
  public void onRepositoryChange(String namespace, Properties newProperties) {
    if (newProperties.equals(m_fileProperties)) {
      return;
    }
    Properties newFileProperties = new Properties();
    newFileProperties.putAll(newProperties);
    updateFileProperties(newFileProperties);
    this.fireRepositoryChange(namespace, newProperties);
  }

  @Override
  protected void sync() {
    Transaction transaction = Cat.newTransaction("Apollo.ConfigService", "queryLocalConfigFile");
    Throwable exception = null;
    try {
      transaction.addData("Basedir", m_baseDir.getAbsolutePath());
      m_fileProperties = this.loadFromLocalCacheFile(m_baseDir, m_namespace);
      transaction.setStatus(Message.SUCCESS);
    } catch (Throwable ex) {
      Cat.logError(ex);
      transaction.setStatus(ex);
      exception = ex;
      //ignore
    } finally {
      transaction.complete();
    }

    //sync with fallback immediately
    trySyncFromFallback();

    if (m_fileProperties == null) {
      throw new RuntimeException(
          "Load config from local config failed!", exception);
    }
  }

  private void trySyncFromFallback() {
    if (m_fallback == null) {
      return;
    }
    try {
      Properties properties = m_fallback.getConfig();
      updateFileProperties(properties);
    } catch (Throwable ex) {
      Cat.logError(ex);
      logger
          .warn("Sync config from fallback repository {} failed, reason: {}", m_fallback.getClass(),
              ExceptionUtil.getDetailMessage(ex));
    }
  }

  private synchronized void updateFileProperties(Properties newProperties) {
    if (newProperties.equals(m_fileProperties)) {
      return;
    }
    this.m_fileProperties = newProperties;
    persistLocalCacheFile(m_baseDir, m_namespace);
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
      } catch (IOException ex) {
        Cat.logError(ex);
        throw new RuntimeException(String
            .format("Loading config from local cache file %s failed", file.getAbsolutePath()), ex);
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException ex) {
          // ignore
        }
      }
    } else {
      throw new RuntimeException(
          String.format("Cannot read from local cache file %s", file.getAbsolutePath()));
    }

    return properties;
  }

  void persistLocalCacheFile(File baseDir, String namespace) {
    if (baseDir == null) {
      return;
    }
    File file = assembleLocalCacheFile(baseDir, namespace);

    OutputStream out = null;

    Transaction transaction = Cat.newTransaction("Apollo.ConfigService", "persistLocalConfigFile");
    transaction.addData("LocalConfigFile", file.getAbsolutePath());
    try {
      out = new FileOutputStream(file);
      m_fileProperties.store(out, "Persisted by DefaultConfig");
      transaction.setStatus(Message.SUCCESS);
    } catch (IOException ex) {
      Cat.logError(ex);
      transaction.setStatus(ex);
      logger.warn("Persist local cache file {} failed, reason: {}.", file.getAbsolutePath(),
          ExceptionUtil.getDetailMessage(ex));
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException ex) {
          //ignore
        }
      }
      transaction.complete();
    }
  }

  File assembleLocalCacheFile(File baseDir, String namespace) {

    String fileName =
        String.format("%s.properties", Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
            .join(m_configUtil.getAppId(), m_configUtil.getCluster(), namespace));
    return new File(baseDir, fileName);
  }
}
