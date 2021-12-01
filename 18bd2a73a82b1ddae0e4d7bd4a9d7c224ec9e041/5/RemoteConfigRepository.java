package com.ctrip.apollo.internals;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.http.HttpRequest;
import com.ctrip.apollo.util.http.HttpResponse;
import com.ctrip.apollo.util.http.HttpUtil;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerLoader;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigRepository implements ConfigRepository {
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfigRepository.class);
  private PlexusContainer m_container;
  private ConfigServiceLocator m_serviceLocator;
  private HttpUtil m_httpUtil;
  private ConfigUtil m_configUtil;
  private AtomicReference<ApolloConfig> m_configCache;
  private String m_namespace;

  public RemoteConfigRepository(String namespace) {
    m_namespace = namespace;
    m_configCache = new AtomicReference<>();
    m_container = ContainerLoader.getDefaultContainer();
    try {
      m_configUtil = m_container.lookup(ConfigUtil.class);
      m_httpUtil = m_container.lookup(HttpUtil.class);
      m_serviceLocator = m_container.lookup(ConfigServiceLocator.class);
    } catch (ComponentLookupException e) {
      throw new IllegalStateException("Unable to load component!", e);
    }
  }

  @Override
  public Properties loadConfig() {
    if (m_configCache.get() == null) {
      initRemoteConfig();
    }
    return transformApolloConfigToProperties(m_configCache.get());
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    //remote config doesn't need fallback
  }

  private void initRemoteConfig() {
    m_configCache.set(this.loadApolloConfig());
  }

  private Properties transformApolloConfigToProperties(ApolloConfig apolloConfig) {
    Properties result = new Properties();
    result.putAll(apolloConfig.getConfigurations());
    return result;
  }


  private ApolloConfig loadApolloConfig() {
    String appId = m_configUtil.getAppId();
    String cluster = m_configUtil.getCluster();
    String uri = getConfigServiceUrl();

    logger.info("Loading config from {}, appId={}, cluster={}, namespace={}", uri, appId, cluster,
        m_namespace);
    HttpRequest request =
        new HttpRequest(assembleUrl(uri, appId, cluster, m_namespace, m_configCache.get()));

    try {
      HttpResponse<ApolloConfig> response = m_httpUtil.doGet(request, ApolloConfig.class);
      if (response.getStatusCode() == 304) {
        return m_configCache.get();
      }

      logger.info("Loaded config: {}", response.getBody());

      return response.getBody();
    } catch (Throwable t) {
      String message =
          String.format("Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s", appId,
              cluster, m_namespace);
      logger.error(message, t);
      throw new RuntimeException(message, t);
    }
  }

  private String assembleUrl(String uri, String appId, String cluster, String namespace,
                             ApolloConfig previousConfig) {
    String path = "/config/%s/%s";
    List<String> params = Lists.newArrayList(appId, cluster);

    if (!Strings.isNullOrEmpty(namespace)) {
      path = path + "/%s";
      params.add(namespace);
    }
    if (previousConfig != null) {
      path = path + "?releaseId=%s";
      params.add(String.valueOf(previousConfig.getReleaseId()));
    }

    String pathExpanded = String.format(path, params.toArray());
    return uri + pathExpanded;
  }

  private String getConfigServiceUrl() {
    List<ServiceDTO> services = m_serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new RuntimeException("No available config service");
    }
    return services.get(0).getHomepageUrl();
  }
}
