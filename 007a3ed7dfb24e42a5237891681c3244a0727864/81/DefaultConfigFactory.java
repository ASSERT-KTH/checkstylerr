package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.RemoteConfigRepository;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

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
    Transaction transaction = Cat.newTransaction("Apollo.ConfigService", "createLocalConfigDir");
    transaction.addData("BaseDir", baseDir.getAbsolutePath());
    try {
      Files.createDirectory(baseDir.toPath());
      transaction.setStatus(Message.SUCCESS);
    } catch (IOException ex) {
      Cat.logError(ex);
      transaction.setStatus(ex);
      logger.warn(
          "Unable to create local config cache directory {}, reason: {}. Will not able to cache config file.",
          baseDir, ExceptionUtil.getDetailMessage(ex));
    } finally {
      transaction.complete();
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
