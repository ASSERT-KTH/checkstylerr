package com.ctrip.apollo.spi;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactoryManager {
  public ConfigFactory getFactory(String namespace);
}
