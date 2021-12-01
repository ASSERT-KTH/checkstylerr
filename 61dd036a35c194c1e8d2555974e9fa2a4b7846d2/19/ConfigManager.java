package com.ctrip.apollo.client.manager;

import com.ctrip.apollo.client.config.Config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigManager {
  /**
   * get config according to the namespace
   * @param namespace the namespace of the config
   * @return config instance
   */
  Config findOrCreate(String namespace);
}
