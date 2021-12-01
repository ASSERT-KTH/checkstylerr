package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.RemoteConfigRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Named;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigFactory.class)
public class DefaultConfigFactory implements ConfigFactory {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConfigFactory.class);

  @Override
  public Config create(String namespace) {
    DefaultConfig defaultConfig =
        new DefaultConfig(namespace, createLocalConfigRepository(namespace));
    return defaultConfig;
  }

  LocalFileConfigRepository createLocalConfigRepository(String namespace) {
    LocalFileConfigRepository localFileConfigRepository =
        new LocalFileConfigRepository(namespace);
    localFileConfigRepository.setUpstreamRepository(createRemoteConfigRepository(namespace));
    return localFileConfigRepository;
  }

  RemoteConfigRepository createRemoteConfigRepository(String namespace) {
    return new RemoteConfigRepository(namespace);
  }
}
