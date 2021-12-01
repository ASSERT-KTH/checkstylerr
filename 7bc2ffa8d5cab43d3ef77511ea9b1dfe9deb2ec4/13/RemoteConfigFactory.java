package com.ctrip.apollo.client.factory.impl;

import com.ctrip.apollo.client.config.Config;
import com.ctrip.apollo.client.config.impl.RemoteConfig;
import com.ctrip.apollo.client.factory.ConfigFactory;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigFactory implements ConfigFactory {
  private static final RemoteConfigFactory instance = new RemoteConfigFactory();

  private RemoteConfigFactory() {
  }

  public static RemoteConfigFactory getInstance() {
    return instance;
  }

  @Override
  public Config createConfig(String namespace) {
    return new RemoteConfig(RemoteConfigLoaderFactory.getInstance().createConfigLoader(), namespace);
  }
}
