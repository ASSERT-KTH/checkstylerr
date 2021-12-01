package com.ctrip.apollo.client.manager.impl;

import com.ctrip.apollo.client.factory.ConfigFactory;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigManager extends AbstractConfigManager{
  public RemoteConfigManager(ConfigFactory configFactory) {
    super(configFactory);
  }
}
