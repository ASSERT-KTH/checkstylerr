package com.ctrip.apollo.client.loader.impl;

import com.google.common.collect.Maps;

import com.ctrip.apollo.client.loader.ConfigServiceLocator;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ServiceDTO;

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

/**
 * Load config from remote config server
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigLoader extends AbstractConfigLoader {
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfigLoader.class);
  private final RestTemplate restTemplate;
  private final ConfigServiceLocator serviceLocator;

  public RemoteConfigLoader(RestTemplate restTemplate,
                            ConfigServiceLocator locator) {
    this.restTemplate = restTemplate;
    this.serviceLocator = locator;
  }

  ApolloConfig getRemoteConfig(RestTemplate restTemplate, String uri,
                               ApolloRegistry apolloRegistry, ApolloConfig previousConfig) {
    String appId = apolloRegistry.getAppId();
    String cluster = apolloRegistry.getClusterName();
    String namespace = apolloRegistry.getNamespace();

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

  @Override
  protected ApolloConfig doLoadApolloConfig(ApolloRegistry apolloRegistry, ApolloConfig previous) {
    ApolloConfig result = this.getRemoteConfig(restTemplate,
        getConfigServiceUrl(), apolloRegistry, previous);
    //When remote server return 304, we need to return the previous result
    return result == null ? previous : result;
  }

  private String getConfigServiceUrl() {
    List<ServiceDTO> services = serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new RuntimeException("No available config service");
    }
    return services.get(0).getHomepageUrl();
  }
}
