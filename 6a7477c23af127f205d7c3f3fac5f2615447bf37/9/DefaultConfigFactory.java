package com.ctrip.apollo.spi;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.apollo.internals.DefaultConfig;
import com.ctrip.apollo.internals.LocalFileConfigRepository;
import com.ctrip.apollo.internals.RemoteConfigRepository;
import com.dianping.cat.Cat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Named;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigFactory.class, value = "default")
public class DefaultConfigFactory implements ConfigFactory {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConfigFactory.class);
  private static final String CONFIG_DIR = "/config-cache";
  private File m_baseDir;

  /**
   * Create the config factory.
   */
  public DefaultConfigFactory() {
    m_baseDir = new File(ClassLoaderUtil.getClassPath() + CONFIG_DIR);
    this.checkLocalConfigCacheDir(m_baseDir);
  }

  private void checkLocalConfigCacheDir(File baseDir) {
    if (baseDir.exists()) {
      return;
    }
    try {
      Files.createDirectory(baseDir.toPath());
    } catch (IOException ex) {
      Cat.logError(ex);
      logger.error("Unable to create directory: " + baseDir, ex);
    }
  }

  @Override
  public Config create(String namespace) {
    DefaultConfig defaultConfig =
        new DefaultConfig(namespace, createLocalConfigRepository(namespace));
    return defaultConfig;
  }

  LocalFileConfigRepository createLocalConfigRepository(String namespace) {
    LocalFileConfigRepository localFileConfigRepository =
        new LocalFileConfigRepository(m_baseDir, namespace);
    localFileConfigRepository.setFallback(createRemoteConfigRepository(namespace));
    return localFileConfigRepository;
  }

  RemoteConfigRepository createRemoteConfigRepository(String namespace) {
    return new RemoteConfigRepository(namespace);
  }
}
