package com.ctrip.apollo.client.factory.impl;

import com.ctrip.apollo.client.factory.ConfigLoaderFactory;
import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.loader.ConfigServiceLocator;
import com.ctrip.apollo.client.loader.impl.RemoteConfigLoader;

import org.springframework.web.client.RestTemplate;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigLoaderFactory implements ConfigLoaderFactory {
  private static RemoteConfigLoaderFactory configLoaderFactory = new RemoteConfigLoaderFactory();

  private RemoteConfigLoaderFactory() {
  }

  public static RemoteConfigLoaderFactory getInstance() {
    return configLoaderFactory;
  }

  @Override
  public ConfigLoader createConfigLoader() {
    ConfigLoader
        remoteConfigLoader =
        new RemoteConfigLoader(new RestTemplate(), new ConfigServiceLocator());
    return remoteConfigLoader;
  }
}
