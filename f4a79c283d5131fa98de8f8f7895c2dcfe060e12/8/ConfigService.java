package com.ctrip.apollo;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.internals.ConfigManager;
import com.ctrip.apollo.spi.ConfigFactory;
import com.ctrip.apollo.spi.ConfigRegistry;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

/**
 * Entry point for client config use
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigService {
  private static final ConfigService s_instance = new ConfigService();

  private PlexusContainer m_container;

  private ConfigService() {
    m_container = ContainerLoader.getDefaultContainer();
  }

  /**
   * Get the config instance with default namespace
   * @return config instance
   */
  public static Config getConfig() {
    return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * Get the config instance for the namespace
   * @param namespace the namespace of the config
   * @return config instance
   */
  public static Config getConfig(String namespace) {
    return getManager().getConfig(namespace);
  }

  private static ConfigManager getManager() {
    try {
      return s_instance.m_container.lookup(ConfigManager.class);
    } catch (ComponentLookupException e) {
      throw new IllegalStateException("Unable to load ConfigManager!", e);
    }
  }

  private static ConfigRegistry getRegistry() {
    try {
      return s_instance.m_container.lookup(ConfigRegistry.class);
    } catch (ComponentLookupException e) {
      throw new IllegalStateException("Unable to load ConfigRegistry!", e);
    }
  }

  public static void setConfig(Config config) {
    setConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
  }

  public static void setConfig(String namespace, final Config config) {
    getRegistry().register(namespace, new ConfigFactory() {
      @Override
      public Config create(String namespace) {
        return config;
      }
    });
  }

  public static void setConfigFactory(ConfigFactory factory) {
    setConfigFactory(ConfigConsts.NAMESPACE_APPLICATION, factory);
  }

  public static void setConfigFactory(String namespace, ConfigFactory factory) {
    getRegistry().register(namespace, factory);
  }

  // for test only
  public static void setContainer(PlexusContainer m_container) {
    s_instance.m_container = m_container;
  }
}
