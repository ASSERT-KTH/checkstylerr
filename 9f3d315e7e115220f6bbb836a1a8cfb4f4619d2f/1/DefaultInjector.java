package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.spi.ApolloInjectorCustomizer;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigFactoryManager;
import com.ctrip.framework.apollo.spi.ConfigRegistry;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryManager;
import com.ctrip.framework.apollo.spi.DefaultConfigRegistry;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.factory.DefaultPropertiesFactory;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.ctrip.framework.apollo.util.http.DefaultHttpClient;
import com.ctrip.framework.apollo.util.http.HttpClient;

import com.ctrip.framework.apollo.util.yaml.YamlParser;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import java.util.List;

/**
 * Guice injector
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultInjector implements Injector {
  private final com.google.inject.Injector m_injector;
  private final List<ApolloInjectorCustomizer> m_customizers;

  public DefaultInjector() {
    try {
      m_injector = Guice.createInjector(new ApolloModule());
      m_customizers = ServiceBootstrap.loadAllOrdered(ApolloInjectorCustomizer.class);
    } catch (Throwable ex) {
      ApolloConfigException exception = new ApolloConfigException("Unable to initialize Guice Injector!", ex);
      Tracer.logError(exception);
      throw exception;
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    try {
      for (ApolloInjectorCustomizer customizer : m_customizers) {
        T instance = customizer.getInstance(clazz);
        if (instance != null) {
          return instance;
        }
      }
      return m_injector.getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for %s!", clazz.getName()), ex);
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    try {
      for (ApolloInjectorCustomizer customizer : m_customizers) {
        T instance = customizer.getInstance(clazz, name);
        if (instance != null) {
          return instance;
        }
      }
      //Guice does not support get instance by type and name
      return null;
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for %s with name %s!", clazz.getName(), name), ex);
    }
  }

  private static class ApolloModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
      bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
      bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);
      bind(ConfigFactory.class).to(DefaultConfigFactory.class).in(Singleton.class);
      bind(ConfigUtil.class).in(Singleton.class);
      bind(HttpClient.class).to(DefaultHttpClient.class).in(Singleton.class);
      bind(ConfigServiceLocator.class).in(Singleton.class);
      bind(RemoteConfigLongPollService.class).in(Singleton.class);
      bind(YamlParser.class).in(Singleton.class);
      bind(PropertiesFactory.class).to(DefaultPropertiesFactory.class).in(Singleton.class);
    }
  }
}
