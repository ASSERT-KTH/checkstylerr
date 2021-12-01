package com.ctrip.apollo.spi;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigRegistry {
  public void register(String namespace, ConfigFactory factory);

  public ConfigFactory getFactory(String namespace);
}
