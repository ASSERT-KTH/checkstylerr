package com.ctrip.apollo.spi;

import com.ctrip.apollo.Config;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.apollo.internals.ConfigServiceLocator;
import com.ctrip.apollo.internals.DefaultConfig;
import com.ctrip.apollo.internals.RemoteConfig;

import org.springframework.web.client.RestTemplate;
import org.unidal.lookup.annotation.Named;

import java.io.File;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigFactory.class, value = "default")
public class DefaultConfigFactory implements ConfigFactory {
  private static final String CONFIG_DIR = "/config-cache";
  private File m_baseDir;

  public DefaultConfigFactory() {
    m_baseDir = new File(ClassLoaderUtil.getClassPath() + CONFIG_DIR);
  }

  @Override
  public Config create(String namespace) {
    RemoteConfig remoteConfig = this.createRemoteConfig(namespace);
    DefaultConfig defaultConfig = new DefaultConfig(m_baseDir, namespace, remoteConfig);
    return defaultConfig;
  }

  public RemoteConfig createRemoteConfig(String namespace) {
    return new RemoteConfig(new RestTemplate(), new ConfigServiceLocator(), namespace);
  }
}
