package com.ctrip.apollo.client.factory.impl;

import com.ctrip.apollo.client.factory.ConfigLoaderFactory;
import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.loader.ConfigServiceLocator;
import com.ctrip.apollo.client.loader.impl.LocalFileConfigLoader;
import com.ctrip.apollo.client.loader.impl.RemoteConfigLoader;

import org.springframework.web.client.RestTemplate;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalConfigLoaderFactory implements ConfigLoaderFactory {
  private static LocalConfigLoaderFactory instance = new LocalConfigLoaderFactory();

  private LocalConfigLoaderFactory() {
  }

  public static LocalConfigLoaderFactory getInstance() {
    return instance;
  }

  @Override
  public ConfigLoader createConfigLoader() {
    ConfigLoader configLoader = new LocalFileConfigLoader();
    return configLoader;
  }
}
