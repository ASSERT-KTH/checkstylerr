package com.ctrip.framework.apollo;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {
  /**
   * Return the property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value
   */
  public String getProperty(String key, String defaultValue);

  /**
   * Return the integer property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as integer
   *
   * @throws NumberFormatException if the property value is invalid
   */
  public Integer getIntProperty(String key, Integer defaultValue);

  /**
   * Return the long property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as long
   *
   * @throws NumberFormatException if the property value is invalid
   */
  public Long getLongProperty(String key, Long defaultValue);

  /**
   * Return the short property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as short
   *
   * @throws NumberFormatException if the property value is invalid
   */
  public Short getShortProperty(String key, Short defaultValue);

  /**
   * Return the float property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as float
   *
   * @throws NumberFormatException if the property value is invalid
   */
  public Float getFloatProperty(String key, Float defaultValue);

  /**
   * Return the double property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as double
   *
   * @throws NumberFormatException if the property value is invalid
   */
  public Double getDoubleProperty(String key, Double defaultValue);

  /**
   * Return the byte property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as byte
   *
   * @throws NumberFormatException if the property value is invalid
   */
  public Byte getByteProperty(String key, Byte defaultValue);

  /**
   * Return the boolean property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   * @param key the property name
   * @param defaultValue the default value is key is not found
   * @return the property value as boolean
   */
  public Boolean getBooleanProperty(String key, Boolean defaultValue);

  /**
   * Add change listener to this config instance.
   * @param listener the config change listener
   */
  public void addChangeListener(ConfigChangeListener listener);
}
