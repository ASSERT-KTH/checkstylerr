package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
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
    return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
  }

  /**
   * Get the config instance for the namespace.
   * @param namespace the namespace of the config
   * @return config instance
   */
  public static Config getConfig(String namespace) {
    return getManager().getConfig(namespace);
  }

  public static ConfigFile getConfigFile(String namespacePrefix, ConfigFileFormat configFileFormat) {
    return getManager().getConfigFile(namespacePrefix, configFileFormat);
  }

  private static ConfigManager getManager() {
    try {
      return s_instance.m_container.lookup(ConfigManager.class);
    } catch (ComponentLookupException ex) {
      ApolloConfigException exception = new ApolloConfigException("Unable to load ConfigManager!", ex);
      Cat.logError(exception);
      throw exception;
    }
  }

  private static ConfigRegistry getRegistry() {
    try {
      return s_instance.m_container.lookup(ConfigRegistry.class);
    } catch (ComponentLookupException ex) {
      ApolloConfigException exception = new ApolloConfigException("Unable to load ConfigRegistry!", ex);
      Cat.logError(exception);
      throw exception;
    }
  }

  static void setConfig(Config config) {
    setConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
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

      @Override
      public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        return null;
      }

    });
  }

  static void setConfigFactory(ConfigFactory factory) {
    setConfigFactory(ConfigConsts.NAMESPACE_APPLICATION, factory);
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
