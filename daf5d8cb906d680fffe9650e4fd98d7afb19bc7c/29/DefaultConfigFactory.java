package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.PropertiesConfigFile;
import com.ctrip.framework.apollo.internals.RemoteConfigRepository;
import com.ctrip.framework.apollo.internals.XmlConfigFile;

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

  @Override
  public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
    ConfigRepository configRepository = createLocalConfigRepository(namespace);
    switch (configFileFormat) {
      case Properties:
        return new PropertiesConfigFile(namespace, configRepository);
      case XML:
        return new XmlConfigFile(namespace, configRepository);
    }

    return null;
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
