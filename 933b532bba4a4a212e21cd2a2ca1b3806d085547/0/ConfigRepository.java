package com.ctrip.apollo.internals;

import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigRepository {
  public Properties loadConfig();

  public void setFallback(ConfigRepository fallbackConfigRepository);
}
