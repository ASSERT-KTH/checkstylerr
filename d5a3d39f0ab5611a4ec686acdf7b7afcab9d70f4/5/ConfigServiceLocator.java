package com.ctrip.apollo.internals;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.apollo.util.ConfigUtil;
import com.ctrip.apollo.util.http.HttpRequest;
import com.ctrip.apollo.util.http.HttpResponse;
import com.ctrip.apollo.util.http.HttpUtil;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Named(type = ConfigServiceLocator.class)
public class ConfigServiceLocator implements Initializable {
  private static final Logger logger = LoggerFactory.getLogger(ConfigServiceLocator.class);
  @Inject
  private HttpUtil m_httpUtil;
  @Inject
  private ConfigUtil m_configUtil;
  private AtomicReference<List<ServiceDTO>> m_configServices;
  private Type m_responseType;
  private ScheduledExecutorService m_executorService;

  /**
   * Create a config service locator.
   */
  public ConfigServiceLocator() {
    List<ServiceDTO> initial = Lists.newArrayList();
    m_configServices = new AtomicReference<>(initial);
    m_responseType = new TypeToken<List<ServiceDTO>>() {
    }.getType();
    this.m_executorService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("ConfigServiceLocator", true));
  }

  @Override
  public void initialize() throws InitializationException {
    this.tryUpdateConfigServices();
    this.schedulePeriodicRefresh();
  }

  /**
   * Get the config service info from remote meta server.
   *
   * @return the services dto
   */
  public List<ServiceDTO> getConfigServices() {
    if (m_configServices.get().isEmpty()) {
      updateConfigServices();
    }

    return m_configServices.get();
  }

  private boolean tryUpdateConfigServices() {
    try {
      updateConfigServices();
      return true;
    } catch (Throwable ex) {
      //ignore
    }
    return false;
  }

  private void schedulePeriodicRefresh() {
    this.m_executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            logger.debug("refresh config services");
            Transaction transaction = Cat.newTransaction("Apollo.MetaService", "periodicRefresh");
            boolean syncResult = tryUpdateConfigServices();
            String status = syncResult ? Message.SUCCESS : "-1";
            transaction.setStatus(status);
            transaction.complete();
          }
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
        m_configUtil.getRefreshTimeUnit());
  }

  //TODO periodically update config services
  private synchronized void updateConfigServices() {
    String domainName = m_configUtil.getMetaServerDomainName();
    String url = domainName + "/services/config";

    HttpRequest request = new HttpRequest(url);
    int maxRetries = 5;
    Throwable exception = null;

    for (int i = 0; i < maxRetries; i++) {
      Transaction transaction = Cat.newTransaction("Apollo.MetaService", "getConfigService");
      transaction.addData("Url", url);
      try {
        HttpResponse<List<ServiceDTO>> response = m_httpUtil.doGet(request, m_responseType);
        m_configServices.set(response.getBody());
        logConfigServicesToCat(response.getBody());
        transaction.setStatus(Message.SUCCESS);
        return;
      } catch (Throwable ex) {
        Cat.logError(ex);
        transaction.setStatus(ex);
        exception = ex;
      } finally {
        transaction.complete();
      }

      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ex) {
        //ignore
      }
    }

    throw new RuntimeException("Get config services failed", exception);
  }

  private void logConfigServicesToCat(List<ServiceDTO> serviceDtos) {
    for (ServiceDTO serviceDto : serviceDtos) {
      Cat.logEvent("Apollo.Config.Services", serviceDto.getHomepageUrl());
    }
  }
}
