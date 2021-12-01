package com.ctrip.apollo.client.manager.impl;

import com.ctrip.apollo.client.factory.impl.RemoteConfigFactory;
import com.ctrip.apollo.client.manager.ConfigManager;
import com.ctrip.apollo.client.manager.ConfigManagerManager;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigManagerManager implements ConfigManagerManager {
  @Override
  public ConfigManager getConfigManager() {
    return new RemoteConfigManager(RemoteConfigFactory.getInstance());
  }
}
