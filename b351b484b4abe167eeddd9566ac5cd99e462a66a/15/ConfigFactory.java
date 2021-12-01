package com.ctrip.apollo.spi;

import com.ctrip.apollo.Config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactory {
  /**
   * Create the config instance for the namespace.
   *
   * @param namespace the namespace
   * @return the newly created config instance
   */
  public Config create(String namespace);
}
