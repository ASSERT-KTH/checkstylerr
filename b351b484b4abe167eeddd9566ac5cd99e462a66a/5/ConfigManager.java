package com.ctrip.apollo.internals;

import com.ctrip.apollo.Config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigManager {
  /**
   * Get the config instance for the namespace specified.
   * @param namespace the namespace
   * @return the config instance for the namespace
   */
  public Config getConfig(String namespace);
}
