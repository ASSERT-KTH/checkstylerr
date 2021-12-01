package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.loader.ConfigLoader;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.core.dto.ApolloConfig;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigLoader implements ConfigLoader {
  @Override
  public ApolloConfig loadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous) {
    try {
      return doLoadApolloConfig(apolloRegistry, previous);
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Load Apollo Config failed - %s", apolloRegistry.toString()), ex);
    }
  }

  protected abstract ApolloConfig doLoadApolloConfig(ApolloRegistry apolloRegistry,
                                                     ApolloConfig previous);

}
