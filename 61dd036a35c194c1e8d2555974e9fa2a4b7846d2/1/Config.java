package com.ctrip.apollo;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {
  public String getProperty(String key, String defaultValue);
}
