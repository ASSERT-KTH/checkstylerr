package com.ctrip.apollo.internals;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import com.ctrip.apollo.core.dto.ApolloConfig;
import com.ctrip.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.ExceptionUtil;
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
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private final AtomicBoolean m_longPollingStopped;

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
    this.m_longPollingStopped = new AtomicBoolean(false);
    this.m_executorService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("RemoteConfigRepository", true));
    this.trySync();
    this.schedulePeriodicRefresh();
    this.scheduleLongPollingRefresh();
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
    logger.debug("Schedule periodic refresh with interval: {} {}",
        m_configUtil.getRefreshInterval(), m_configUtil.getRefreshTimeUnit());
    this.m_executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            logger.debug("refresh config for namespace: {}", m_namespace);
            Transaction transaction = Cat.newTransaction("Apollo.ConfigService", "periodicRefresh");
            trySync();
            transaction.setStatus(Message.SUCCESS);
            transaction.complete();
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

    logger.debug("Remote Config refreshed!");

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
    Cat.logEvent("Apollo.Client.ConfigInfo",
        String.format("%s-%s-%s", appId, cluster, m_namespace));
    int maxRetries = 2;
    Throwable exception = null;

    List<ServiceDTO> configServices = getConfigServices();
    for (int i = 0; i < maxRetries; i++) {
      List<ServiceDTO> randomConfigServices = Lists.newArrayList(configServices);
      Collections.shuffle(randomConfigServices);

      for (ServiceDTO configService : randomConfigServices) {
        String url =
            assembleQueryConfigUrl(configService.getHomepageUrl(), appId, cluster, m_namespace,
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
      } catch (InterruptedException ex) {
        //ignore
      }
    }
    String message = String.format(
        "Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s, services: %s",
        appId, cluster, m_namespace, configServices);
    throw new RuntimeException(message, exception);
  }

  private String assembleQueryConfigUrl(String uri, String appId, String cluster, String namespace,
                                        ApolloConfig previousConfig) {
    Escaper escaper = UrlEscapers.urlPathSegmentEscaper();
    String path = "configs/%s/%s";
    List<String> params = Lists.newArrayList(escaper.escape(appId), escaper.escape(cluster));

    if (!Strings.isNullOrEmpty(namespace)) {
      path = path + "/%s";
      params.add(escaper.escape(namespace));
    }
    if (previousConfig != null) {
      path = path + "?releaseId=%s";
      params.add(escaper.escape(String.valueOf(previousConfig.getReleaseId())));
    }

    String pathExpanded = String.format(path, params.toArray());
    if (!uri.endsWith("/")) {
      uri += "/";
    }
    return uri + pathExpanded;
  }

  private void scheduleLongPollingRefresh() {
    final String appId = m_configUtil.getAppId();
    final String cluster = m_configUtil.getCluster();
    final ExecutorService longPollingService =
        Executors.newFixedThreadPool(2,
            ApolloThreadFactory.create("RemoteConfigRepository-LongPolling", true));
    longPollingService.submit(new Runnable() {
      @Override
      public void run() {
        doLongPollingRefresh(appId, cluster, longPollingService);
      }
    });
  }

  private void doLongPollingRefresh(String appId, String cluster,
                                    ExecutorService longPollingService) {
    final Random random = new Random();
    ServiceDTO lastServiceDto = null;
    Transaction transaction = null;
    while (!m_longPollingStopped.get() && !Thread.currentThread().isInterrupted()) {
      try {
        if (lastServiceDto == null) {
          List<ServiceDTO> configServices = getConfigServices();
          lastServiceDto = configServices.get(random.nextInt(configServices.size()));
        }

        String url =
            assembleLongPollRefreshUrl(lastServiceDto.getHomepageUrl(), appId, cluster,
                m_namespace, m_configCache.get());

        logger.debug("Long polling from {}", url);
        HttpRequest request = new HttpRequest(url);
        //no timeout for read
        request.setReadTimeout(0);

        transaction = Cat.newTransaction("Apollo.ConfigService", "pollNotification");
        transaction.addData("Url", url);

        HttpResponse<ApolloConfigNotification> response =
            m_httpUtil.doGet(request, ApolloConfigNotification.class);

        logger.debug("Long polling response: {}, url: {}", response.getStatusCode(), url);
        if (response.getStatusCode() == 200) {
          longPollingService.submit(new Runnable() {
            @Override
            public void run() {
              trySync();
            }
          });
        }
        transaction.addData("StatusCode", response.getStatusCode());
        transaction.setStatus(Message.SUCCESS);
      } catch (Throwable ex) {
        logger.warn("Long polling failed for appId: {}, cluster: {}, namespace: {}",
            appId, cluster, m_namespace, ExceptionUtil.getDetailMessage(ex));
        lastServiceDto = null;
        Cat.logError(ex);
        if (transaction != null) {
          transaction.setStatus(ex);
        }
        try {
          TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ie) {
          //ignore
        }
      } finally {
        if (transaction != null) {
          transaction.complete();
        }
      }
    }
  }

  private String assembleLongPollRefreshUrl(String uri, String appId, String cluster,
                                            String namespace,
                                            ApolloConfig previousConfig) {
    Escaper escaper = UrlEscapers.urlPathSegmentEscaper();
    Map<String, String> queryParams = Maps.newHashMap();
    queryParams.put("appId", escaper.escape(appId));
    queryParams.put("cluster", escaper.escape(cluster));

    if (!Strings.isNullOrEmpty(namespace)) {
      queryParams.put("namespace", escaper.escape(namespace));
    }
    if (previousConfig != null) {
      queryParams.put("releaseId", escaper.escape(previousConfig.getReleaseId()));
    }

    String params = Joiner.on("&").withKeyValueSeparator("=").join(queryParams);
    if (!uri.endsWith("/")) {
      uri += "/";
    }

    return uri + "notifications?" + params;
  }

  void stopLongPollingRefresh() {
    this.m_longPollingStopped.compareAndSet(false, true);
  }

  private List<ServiceDTO> getConfigServices() {
    List<ServiceDTO> services = m_serviceLocator.getConfigServices();
    if (services.size() == 0) {
      throw new RuntimeException("No available config service");
    }

    return services;
  }
}
