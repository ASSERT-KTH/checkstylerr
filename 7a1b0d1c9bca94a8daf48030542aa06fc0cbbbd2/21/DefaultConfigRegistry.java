package com.ctrip.apollo.spi;

import com.google.common.collect.Maps;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Named;

import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigRegistry.class, value = "default")
public class DefaultConfigRegistry implements ConfigRegistry, LogEnabled {
  private Map<String, ConfigFactory> m_instances = Maps.newConcurrentMap();

  private Logger m_logger;

  @Override
  public void register(String namespace, ConfigFactory factory) {
    if (m_instances.containsKey(namespace)) {
      m_logger.warn(
          String.format("ConfigFactory(%s) is overridden by %s!", namespace, factory.getClass()));
    }

    m_instances.put(namespace, factory);
  }

  @Override
  public ConfigFactory getFactory(String namespace) {
    ConfigFactory config = m_instances.get(namespace);

    return config;
  }

  @Override
  public void enableLogging(Logger logger) {
    m_logger = logger;
  }
}
