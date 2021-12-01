package com.ctrip.apollo.client.loader;

import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.core.dto.ApolloConfig;

/**
 * @author Jason Song(songs_ctrip.com)
 */
public interface ConfigLoader {
  /**
   * Load apollo config
   * @param apolloRegistry
   * @param previous
   * @return
   */
  ApolloConfig loadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous);
}
