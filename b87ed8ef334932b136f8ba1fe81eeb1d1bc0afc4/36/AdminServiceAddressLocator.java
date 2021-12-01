package com.ctrip.framework.apollo.portal.api;

import com.google.common.collect.Lists;

import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.PortalSettings;
import com.dianping.cat.Cat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

@Component
public class AdminServiceAddressLocator {

  private static final int DEFAULT_TIMEOUT_MS = 1000;
  private static final long REFRESH_INTERVAL = 5 * 60 * 1000;
  private static final int RETRY_TIMES = 3;
  private static final String ADMIN_SERVICE_URL_PATH = "/services/admin";

  private ScheduledExecutorService refreshServiceAddressService;
  private RestTemplate restTemplate;
  private List<Env> allEnvs;
  private Map<Env, List<ServiceDTO>> cache = new ConcurrentHashMap<>();

  @Autowired
  private HttpMessageConverters httpMessageConverters;
  @Autowired
  private PortalSettings portalSettings;

  @PostConstruct
  public void init() {
    allEnvs = portalSettings.getAllEnvs();

    //init restTemplate
    restTemplate = new RestTemplate(httpMessageConverters.getConverters());
    if (restTemplate.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
      SimpleClientHttpRequestFactory rf =
          (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
      rf.setReadTimeout(DEFAULT_TIMEOUT_MS);
      rf.setConnectTimeout(DEFAULT_TIMEOUT_MS);
    } else if (restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
      HttpComponentsClientHttpRequestFactory rf =
          (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
      rf.setReadTimeout(DEFAULT_TIMEOUT_MS);
      rf.setConnectTimeout(DEFAULT_TIMEOUT_MS);
    }

    refreshServiceAddressService = Executors.newScheduledThreadPool(1);

    refreshServiceAddressService.scheduleWithFixedDelay(
        new RefreshAdminServerAddressTask(), 0, REFRESH_INTERVAL,
        TimeUnit.MILLISECONDS);
  }

  public List<ServiceDTO> getServiceList(Env env) {
    List<ServiceDTO> services = cache.get(env);
    if (CollectionUtils.isEmpty(services)) {
      return Collections.emptyList();
    }
    List<ServiceDTO> randomConfigServices = Lists.newArrayList(services);
    Collections.shuffle(randomConfigServices);
    return randomConfigServices;
  }

  //Maintain admin server address
  private class RefreshAdminServerAddressTask implements Runnable {

    @Override
    public void run() {
      for (Env env : allEnvs) {
        refreshServerAddressCache(env);
      }
    }
  }

  private void refreshServerAddressCache(Env env) {

    for (int i = 0; i < RETRY_TIMES; i++) {

      try {
        ServiceDTO[] services = getAdminServerAddress(env);
        if (services == null || services.length == 0) {
          continue;
        }
        cache.put(env, Arrays.asList(services));
        break;
      } catch (Throwable e) {//meta server error
        Cat.logError("get admin server address fail", e);
        continue;
      }
    }
  }

  private ServiceDTO[] getAdminServerAddress(Env env) {
    String domainName = MetaDomainConsts.getDomain(env);
    String url = domainName + ADMIN_SERVICE_URL_PATH;
    return restTemplate.getForObject(url, ServiceDTO[].class);
  }


}
