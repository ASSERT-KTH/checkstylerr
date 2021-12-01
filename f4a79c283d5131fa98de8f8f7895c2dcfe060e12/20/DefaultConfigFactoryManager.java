package com.ctrip.apollo.spi;

import com.google.common.collect.Maps;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.LookupException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigFactoryManager.class, value = "default")
public class DefaultConfigFactoryManager extends ContainerHolder implements ConfigFactoryManager {
  @Inject
  private ConfigRegistry m_registry;

  private Map<String, ConfigFactory> m_factories = Maps.newConcurrentMap();

  @Override
  public ConfigFactory getFactory(String namespace) {
    // step 1: check hacked factory
    ConfigFactory factory = m_registry.getFactory(namespace);

    if (factory != null) {
      return factory;
    }

    // step 2: check cache
    factory = m_factories.get(namespace);

    if (factory != null) {
      return factory;
    }

    // step 3: check declared config factory
    try {
      factory = lookup(ConfigFactory.class, namespace);
    } catch (LookupException e) {
      // ignore it
    }

    // step 4: check default config factory
    if (factory == null) {
      factory = lookup(ConfigFactory.class, "default");
    }

    m_factories.put(namespace, factory);

    // factory should not be null
    return factory;
  }
}
