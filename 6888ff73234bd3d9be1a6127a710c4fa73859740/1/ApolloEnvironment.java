package com.ctrip.apollo.client;

import com.ctrip.apollo.client.model.PropertyChange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;

import java.util.List;

/**
 * Apollo config for non-Spring application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloEnvironment {
  private static final Logger logger = LoggerFactory.getLogger(ApolloEnvironment.class);
  private static ApolloEnvironment instance = new ApolloEnvironment();

  private volatile CompositePropertySource propertySource;
  private ApolloEnvironmentManager apolloEnvironmentManager;

  private ApolloEnvironment() {
    apolloEnvironmentManager = new ApolloEnvironmentManager(this);
  }

  public void init() {
    this.apolloEnvironmentManager.init();
  }

  public static ApolloEnvironment getInstance() {
    return instance;
  }

  /**
   * Return the property value with the given key, or {@code null}
   * if the key doesn't exist.
   *
   * @param key the property name
   * @return the property value
   */
  public String getProperty(String key) {
    if (this.propertySource == null) {
      throw new IllegalStateException(
          "ApolloEnvironment not initialized, please call ApolloEnvironment.init() first");
    }
    Object value = this.propertySource.getProperty(key);
    if (value == null) {
      return null;
    }
    return (String) value;
  }

  /**
   * Return the property value with the given key, or
   * {@code defaultValue} if the key doesn't exist.
   */
  public String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    return value == null ? defaultValue : value;
  }

  void updatePropertySource(CompositePropertySource propertySource) {
    this.propertySource = propertySource;
  }

  void updatePropertySource(CompositePropertySource propertySource, List<PropertyChange> changes) {
    this.updatePropertySource(propertySource);
    //TODO broadcast changes
  }

}
