package com.ctrip.apollo.internals;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.http.HttpRequest;
import com.ctrip.apollo.util.http.HttpResponse;
import com.ctrip.apollo.util.http.HttpUtil;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.ContainerLoader;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigRepository extends AbstractConfigRepository {
  private static final Logger logger = LoggerFactory.getLogger(RemoteConfigRepository.class);
  private PlexusContainer m_container;
  private final ConfigServiceLocator m_serviceLocator;
  private final HttpUtil m_httpUtil;
  private final ConfigUtil m_configUtil;
  private volatile AtomicReference<ApolloConfig> m_configCache;
  private final String m_namespace;
  private final ScheduledExecutorService m_executorService;

  /**
   * Constructor.
   *
   * @param namespace the namespace
   */
  public RemoteConfigRepository(String namespace) {
    m_namespace = namespace;
    m_configCache = new AtomicReference<>();
    m_container = ContainerLoader.getDefaultContainer();
    try {
      m_configUtil = m_container.lookup(ConfigUtil.class);
      m_httpUtil = m_container.lookup(HttpUtil.class);
      m_serviceLocator = m_container.lookup(ConfigServiceLocator.class);
    } catch (ComponentLookupException ex) {
      Cat.logError(ex);
      throw new IllegalStateException("Unable to load component!", ex);
    }
    this.m_executorService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("RemoteConfigRepository", true));
    this.trySync();
    this.schedulePeriodicRefresh();
  }

  @Override
  public Properties getConfig() {
    if (m_configCache.get() == null) {
      this.sync();
    }
    return transformApolloConfigToProperties(m_configCache.get());
  }

  @Override
  public void setFallback(ConfigRepository fallbackConfigRepository) {
    //remote config doesn't need fallback
  }

  private void schedulePeriodicRefresh() {
    logger.info("Schedule periodic refresh with interval: {} {}",
        m_configUtil.getRefreshInterval(), m_configUtil.getRefreshTimeUnit());
    this.m_executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            trySync();
          }
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
        m_configUtil.getRefreshTimeUnit());
  }

  @Override
  protected synchronized void sync() {
    ApolloConfig previous = m_configCache.get();
    ApolloConfig current = loadApolloConfig();

    //HTTP 304, nothing changed
    if (previous == current) {
      return;
    }

    logger.info("Remote Config changes!");

    m_configCache.set(current);

    this.fireRepositoryChange(m_namespace, this.getConfig());
  }

  private Properties transformApolloConfigToProperties(ApolloConfig apolloConfig) {
    Properties result = new Properties();
    result.putAll(apolloConfig.getConfigurations());
    return result;
  }


  private ApolloConfig loadApolloConfig() {
    String appId = m_configUtil.getAppId();
    String cluster = m_configUtil.getCluster();
    Cat.logEvent("Apollo.Client.Config", String.format("%s-%s-%s", appId, cluster, m_namespace));
    int maxRetries = 2;
    Throwable exception = null;

    List<ServiceDTO> configServices = getConfigServices();
    for (int i = 0; i < maxRetries; i++) {
      List<ServiceDTO> randomConfigServices = Lists.newArrayList(configServices);
      Collections.shuffle(randomConfigServices);

      for (ServiceDTO configService : randomConfigServices) {
        String url =
            assembleUrl(configService.getHomepageUrl(), appId, cluster, m_namespace,
                m_configCache.get());

        logger.debug("Loading config from {}", url);
        HttpRequest request = new HttpRequest(url);

        Transaction transaction = Cat.newTransaction("Apollo.ConfigService", "queryConfig");
        transaction.addData("Url", url);
        try {

          HttpResponse<ApolloConfig> response = m_httpUtil.doGet(request, ApolloConfig.class);

          transaction.addData("StatusCode", response.getStatusCode());
          transaction.setStatus(Message.SUCCESS);

          if (response.getStatusCode() == 304) {
            logger.debug("Config server responds with 304 HTTP status code.");
            return m_configCache.get();
          }
          logger.debug("Loaded config: {}", response.getBody());

          return response.getBody();
        } catch (Throwable ex) {
          Cat.logError(ex);
          transaction.setStatus(ex);
          exception = ex;
        } finally {
          transaction.complete();
        }

      }

      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        //ignore
      }
    }
    String message = String.format(
        "Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s, services: %s",
        appId, cluster, m_namespace, configServices);
    logger.error(message, exception);
    throw new RuntimeException(message, exception);
  }

  private String assembleUrl(String uri, String appId, String cluster, String namespace,
                             ApolloConfig previousConfig) {
    String path = "config/%s/%s";
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
    if (!uri.endsWith("/")) {
      uri += "/";
    }
    return uri + pathExpanded;
  }

  private List<ServiceDTO> getConfigServices() {
    List<ServiceDTO> services = m_serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new RuntimeException("No available config service");
    }

    return services;
  }
}
