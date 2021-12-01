package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.core.dto.ApolloConfig;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class InMemoryConfigLoader extends AbstractConfigLoader {

  @Override
  protected ApolloConfig doLoadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous) {
    return null;
  }
}
