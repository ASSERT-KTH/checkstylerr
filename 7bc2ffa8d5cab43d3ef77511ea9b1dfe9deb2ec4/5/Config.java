package com.ctrip.apollo.client.config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {
  /**
   * Return the property value with the given key, or {@code null}
   * if the key doesn't exist.
   *
   * @param key the property name
   * @return the property value
   */
  String getProperty(String key);

  /**
   * Return the property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value
   */
  String getProperty(String key, String defaultValue);
}
