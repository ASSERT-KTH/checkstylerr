package com.ctrip.apollo.client.factory.impl;

import com.ctrip.apollo.client.config.Config;
import com.ctrip.apollo.client.config.impl.LocalFileConfig;
import com.ctrip.apollo.client.factory.ConfigFactory;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalConfigFactory implements ConfigFactory {
  private static final LocalConfigFactory instance = new LocalConfigFactory();

  private LocalConfigFactory() {
  }

  public static LocalConfigFactory getInstance() {
    return instance;
  }

  @Override
  public Config createConfig(String namespace) {
    return new LocalFileConfig(LocalConfigLoaderFactory.getInstance().createConfigLoader(),
        namespace);
  }
}
