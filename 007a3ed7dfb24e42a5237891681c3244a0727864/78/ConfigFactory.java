package com.ctrip.framework.apollo.spi;

import com.ctrip.framework.apollo.Config;

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
