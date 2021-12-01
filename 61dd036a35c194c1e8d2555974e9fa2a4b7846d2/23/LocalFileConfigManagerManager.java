package com.ctrip.apollo.client.manager.impl;

import com.ctrip.apollo.client.factory.impl.LocalConfigFactory;
import com.ctrip.apollo.client.manager.ConfigManager;
import com.ctrip.apollo.client.manager.ConfigManagerManager;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalFileConfigManagerManager implements ConfigManagerManager{
  @Override
  public ConfigManager getConfigManager() {
    return new LocalFileConfigManager(LocalConfigFactory.getInstance());
  }
}
