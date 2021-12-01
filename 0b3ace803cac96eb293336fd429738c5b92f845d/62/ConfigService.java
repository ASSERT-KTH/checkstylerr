package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigRegistry;
import com.dianping.cat.Cat;

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
   * Get Application's config instance.
   * @return config instance
   */
  public static Config getAppConfig() {
    return getConfig(ConfigConsts.NAMESPACE_DEFAULT);
  }

  /**
   * Get the config instance for the namespace.
   * @param namespace the namespace of the config
   * @return config instance
   */
  public static Config getConfig(String namespace) {
    Cat.logEvent("Apollo.Client.Version", Apollo.VERSION);
    return getManager().getConfig(namespace);
  }

  private static ConfigManager getManager() {
    try {
      return s_instance.m_container.lookup(ConfigManager.class);
    } catch (ComponentLookupException ex) {
      Cat.logError(ex);
      throw new IllegalStateException("Unable to load ConfigManager!", ex);
    }
  }

  private static ConfigRegistry getRegistry() {
    try {
      return s_instance.m_container.lookup(ConfigRegistry.class);
    } catch (ComponentLookupException ex) {
      Cat.logError(ex);
      throw new IllegalStateException("Unable to load ConfigRegistry!", ex);
    }
  }

  static void setConfig(Config config) {
    setConfig(ConfigConsts.NAMESPACE_DEFAULT, config);
  }

  /**
   * Manually set the config for the namespace specified, use with caution.
   * @param namespace the namespace
   * @param config the config instance
   */
  static void setConfig(String namespace, final Config config) {
    getRegistry().register(namespace, new ConfigFactory() {
      @Override
      public Config create(String namespace) {
        return config;
      }
    });
  }

  static void setConfigFactory(ConfigFactory factory) {
    setConfigFactory(ConfigConsts.NAMESPACE_DEFAULT, factory);
  }

  /**
   * Manually set the config factory for the namespace specified, use with caution.
   * @param namespace the namespace
   * @param factory the factory instance
   */
  static void setConfigFactory(String namespace, ConfigFactory factory) {
    getRegistry().register(namespace, factory);
  }

  // for test only
  static void setContainer(PlexusContainer m_container) {
    s_instance.m_container = m_container;
  }
}
