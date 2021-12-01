package com.ctrip.apollo.client.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.ctrip.apollo.client.enums.PropertyChangeType;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.client.model.PropertyChange;
import com.ctrip.apollo.client.model.PropertySourceReloadResult;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigLoaderManager {
  public static final String APOLLO_PROPERTY_SOURCE_NAME = "ApolloConfigProperties";
  private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderManager.class);
  private ConfigLoader configLoader;
  private ConfigUtil configUtil;
  private final ExecutorService executorService;
  private Map<ApolloRegistry, ApolloConfig> currentApolloRegistryConfigCache;
  private Map<ApolloRegistry, ApolloConfig> previousApolloRegistryConfigCache;
  private List<ApolloRegistry> apolloRegistries;

  public ConfigLoaderManager(ConfigLoader configLoader, ConfigUtil configUtil) {
    this.configLoader = configLoader;
    this.configUtil = configUtil;
    this.executorService =
        Executors.newFixedThreadPool(5, ApolloThreadFactory.create("ConfigLoaderManager", true));
    this.currentApolloRegistryConfigCache = Maps.newConcurrentMap();
  }

  public CompositePropertySource loadPropertySource() {
    try {
      apolloRegistries = configUtil.loadApolloRegistries();
    } catch (IOException ex) {
      throw new RuntimeException("Load apollo config registry failed", ex);
    }

    return loadPropertySourceWithApolloRegistries(apolloRegistries);
  }

  public PropertySourceReloadResult reloadPropertySource() {
    CompositePropertySource composite = loadPropertySourceWithApolloRegistries(apolloRegistries);
    List<ApolloConfig> previous =
        Lists.newArrayList(this.previousApolloRegistryConfigCache.values());
    List<ApolloConfig> current = Lists.newArrayList(this.currentApolloRegistryConfigCache.values());
    return new PropertySourceReloadResult(composite, calcPropertyChanges(previous, current));
  }

  /**
   * Load property source with apollo registries provided Should not be invoked in parallel since
   * there are some operations like create/destroy cache, writing to files etc.
   */
  private synchronized CompositePropertySource loadPropertySourceWithApolloRegistries(
      List<ApolloRegistry> apolloRegistries) {
    resetApolloRegistryConfigCache();
    CompositePropertySource composite = new CompositePropertySource(APOLLO_PROPERTY_SOURCE_NAME);
    if (apolloRegistries == null || apolloRegistries.isEmpty()) {
      logger.warn("No Apollo Registry found!");
      return composite;
    }
    try {
      List<ApolloConfig> apolloConfigList = loadApolloConfigs(apolloRegistries);

      Collections.sort(apolloConfigList);
      for (ApolloConfig apolloConfig : apolloConfigList) {
        composite.addPropertySource(new MapPropertySource(assemblePropertySourceName(apolloConfig),
            apolloConfig.getConfigurations()));
      }
      return composite;
    } catch (Throwable throwable) {
      throw new RuntimeException("Load apollo configs failed", throwable);
    }
  }

  List<PropertyChange> calcPropertyChanges(List<ApolloConfig> previous,
      List<ApolloConfig> current) {
    Map<String, Object> previousMap = collectConfigurations(previous);
    Map<String, Object> currentMap = collectConfigurations(current);

    Set<String> previousKeys = previousMap.keySet();
    Set<String> currentKeys = currentMap.keySet();

    Set<String> commonKeys = Sets.intersection(previousKeys, currentKeys);
    Set<String> newKeys = Sets.difference(currentKeys, commonKeys);
    Set<String> removedKeys = Sets.difference(previousKeys, commonKeys);

    List<PropertyChange> changes = Lists.newArrayList();

    for (String newKey : newKeys) {
      changes.add(new PropertyChange(newKey, null, currentMap.get(newKey), PropertyChangeType.NEW));
    }

    for (String removedKey : removedKeys) {
      changes.add(new PropertyChange(removedKey, previousMap.get(removedKey), null,
          PropertyChangeType.DELETED));
    }

    for (String commonKey : commonKeys) {
      if (previousMap.get(commonKey).equals(currentMap.get(commonKey))) {
        continue;
      }
      changes.add(new PropertyChange(commonKey, previousMap.get(commonKey),
          currentMap.get(commonKey), PropertyChangeType.MODIFIED));
    }

    return changes;
  }

  Map<String, Object> collectConfigurations(List<ApolloConfig> apolloConfigs) {
    Collections.sort(apolloConfigs);
    Map<String, Object> configMap = Maps.newHashMap();
    for (int i = apolloConfigs.size() - 1; i > -1; i--) {
      configMap.putAll(apolloConfigs.get(i).getConfigurations());
    }
    return configMap;
  }

  List<ApolloConfig> loadApolloConfigs(List<ApolloRegistry> apolloRegistries) throws Throwable {
    List<Future<ApolloConfig>> futures = Lists.newArrayList();
    for (final ApolloRegistry apolloRegistry : apolloRegistries) {
      futures.add(executorService.submit(new Callable<ApolloConfig>() {
        @Override
        public ApolloConfig call() throws Exception {
          return loadSingleApolloConfig(apolloRegistry);
        }
      }));
    }
    List<ApolloConfig> apolloConfigList = Lists.newArrayList();
    for (Future<ApolloConfig> future : futures) {
      try {
        ApolloConfig result = future.get();
        if (result == null) {
          continue;
        }
        apolloConfigList.add(result);
      } catch (ExecutionException ex) {
        throw ex.getCause();
      }
    }
    return apolloConfigList;
  }

  ApolloConfig loadSingleApolloConfig(ApolloRegistry apolloRegistry) {
    ApolloConfig result =
        configLoader.loadApolloConfig(apolloRegistry, getPreviousApolloConfig(apolloRegistry));
    if (result == null) {
      logger.error("Loaded config null...");
      return null;
    }
    logger.info("Loaded config: {}", result);
    updateCurrentApolloConfigCache(apolloRegistry, result);

    return result;
  }

  void resetApolloRegistryConfigCache() {
    this.previousApolloRegistryConfigCache = currentApolloRegistryConfigCache;
    this.currentApolloRegistryConfigCache = Maps.newConcurrentMap();
  }

  ApolloConfig getPreviousApolloConfig(ApolloRegistry apolloRegistry) {
    return previousApolloRegistryConfigCache.get(apolloRegistry);
  }

  void updateCurrentApolloConfigCache(ApolloRegistry apolloRegistry, ApolloConfig apolloConfig) {
    currentApolloRegistryConfigCache.put(apolloRegistry, apolloConfig);
  }

  private String assemblePropertySourceName(ApolloConfig apolloConfig) {
    return String.format("%d-%s-%s-%d", apolloConfig.getAppId(), apolloConfig.getCluster(),
        apolloConfig.getVersion(), apolloConfig.getReleaseId());
  }
}
