package com.ctrip.apollo.client.loader.impl;

import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.core.dto.ApolloConfig;

/**
 * Load config from local backup file
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class LocalFileConfigLoader extends AbstractConfigLoader {
  @Override
  public ApolloConfig doLoadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous) {
    return null;
  }
}
