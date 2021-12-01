package com.ctrip.apollo.internals;

import com.google.common.collect.Maps;

import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.util.ConfigUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigRepository implements ConfigRepository {
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfigRepository.class);
  private RestTemplate m_restTemplate;
  private ConfigServiceLocator m_serviceLocator;
  private ConfigUtil m_configUtil;
  private Properties m_remoteProperties;
  private String m_namespace;

  public RemoteConfigRepository(RestTemplate restTemplate,
                                ConfigServiceLocator serviceLocator,
                                ConfigUtil configUtil, String namespace) {
    m_restTemplate = restTemplate;
    m_serviceLocator = serviceLocator;
    m_configUtil = configUtil;
    m_namespace = namespace;
  }

  @Override
  public Properties loadConfig() {
    if (m_remoteProperties == null) {
      initRemoteConfig();
    }
    Properties result = new Properties();
    result.putAll(m_remoteProperties);
    return result;
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    //remote config doesn't need fallback
  }

  private void initRemoteConfig() {
    ApolloConfig apolloConfig = this.loadApolloConfig();
    m_remoteProperties = new Properties();
    m_remoteProperties.putAll(apolloConfig.getConfigurations());
  }


  private ApolloConfig loadApolloConfig() {
    String appId = m_configUtil.getAppId();
    String cluster = m_configUtil.getCluster();
    try {
      ApolloConfig result =
          this.getRemoteConfig(m_restTemplate, getConfigServiceUrl(),
              appId, cluster,
              m_namespace,
              null);
      if (result == null) {
        return null;
      }
      logger.info("Loaded config: {}", result);
      return result;
    } catch (Throwable ex) {
      throw new RuntimeException(
          String.format("Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s", appId,
              cluster, m_namespace), ex);
    }
  }


  private ApolloConfig getRemoteConfig(RestTemplate restTemplate, String uri,
                                       String appId, String cluster, String namespace,
                                       ApolloConfig previousConfig) {

    logger.info("Loading config from {}, appId={}, cluster={}, namespace={}", uri, appId, cluster,
        namespace);
    String path = "/config/{appId}/{cluster}";
    Map<String, Object> paramMap = Maps.newHashMap();
    paramMap.put("appId", appId);
    paramMap.put("cluster", cluster);

    if (StringUtils.hasText(namespace)) {
      path = path + "/{namespace}";
      paramMap.put("namespace", namespace);
    }
    if (previousConfig != null) {
      path = path + "?releaseId={releaseId}";
      paramMap.put("releaseId", previousConfig.getReleaseId());
    }

    ResponseEntity<ApolloConfig> response;

    try {
      // TODO retry
      response = restTemplate.exchange(uri
          + path, HttpMethod.GET, new HttpEntity<Void>((Void) null), ApolloConfig.class, paramMap);
    } catch (Throwable ex) {
      throw ex;
    }

    if (response == null) {
      throw new RuntimeException("Load apollo config failed, response is null");
    }

    if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
      return null;
    }

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new RuntimeException(
          String.format("Load apollo config failed, response status %s", response.getStatusCode()));
    }

    ApolloConfig result = response.getBody();
    return result;
  }

  private String getConfigServiceUrl() {
    List<ServiceDTO> services = m_serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new RuntimeException("No available config service");
    }
    return services.get(0).getHomepageUrl();
  }
}
