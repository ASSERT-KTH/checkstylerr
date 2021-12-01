package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.core.dto.ApolloConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigLoader implements ConfigLoader {
  private static final Logger logger = LoggerFactory.getLogger(AbstractConfigLoader.class);
  private ConfigLoader fallback;

  @Override
  public ApolloConfig loadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous) {
    try {
      return doLoadApolloConfig(apolloRegistry, previous);
    } catch (Throwable ex) {
      if (this.fallback == null) {
        throw new RuntimeException(
            String.format("Load Apollo Config failed - %s", apolloRegistry.toString()), ex);
      }
      logger.error("Load Config via {} failed, try to use its fallback {} to load",
          getClass().getSimpleName(), fallback.getClass().getSimpleName(), ex);
      return this.fallback.loadApolloConfig(apolloRegistry, previous);
    }
  }

  protected abstract ApolloConfig doLoadApolloConfig(ApolloRegistry apolloRegistry,
                                                     ApolloConfig previous);

  @Override
  public void setFallBackLoader(ConfigLoader configLoader) {
    this.fallback = configLoader;
  }
}
