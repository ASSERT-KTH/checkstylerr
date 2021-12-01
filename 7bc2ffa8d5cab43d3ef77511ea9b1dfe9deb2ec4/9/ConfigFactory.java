package com.ctrip.apollo.client.factory;

import com.ctrip.apollo.client.config.Config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactory {
  /**
   * create config instance for the namespace
   * @param namespace
   * @return
   */
  Config createConfig(String namespace);
}
