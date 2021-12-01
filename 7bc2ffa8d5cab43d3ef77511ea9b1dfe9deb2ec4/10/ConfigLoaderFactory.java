package com.ctrip.apollo.client.factory;

import com.ctrip.apollo.client.loader.ConfigLoader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigLoaderFactory {
  /**
   * create config loader
   * @return
   */
  ConfigLoader createConfigLoader();
}
