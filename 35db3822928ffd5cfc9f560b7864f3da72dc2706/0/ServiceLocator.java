package com.ctrip.apollo.portal.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.ServiceException;

/**
 * @author liuym
 */
@Service
public class ServiceLocator {

  private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);

  private static final int DEFAULT_TIMEOUT_MS = 1000;

  private RestTemplate restTemplate = new RestTemplate();

  private Map<Env, List<ServiceDTO>> serviceCaches = new ConcurrentHashMap<Env, List<ServiceDTO>>();

  private final AtomicInteger adminCallCounts = new AtomicInteger(0);

  private final AtomicInteger configCallCounts = new AtomicInteger(0);

  public ServiceDTO getAdminService(Env env) throws ServiceException {
    List<ServiceDTO> services = getServices(env, "admin");
    if (services == null || services.size() == 0) {
      throw new ServiceException("No available admin service");
    }
    return services.get(Math.abs(adminCallCounts.getAndIncrement()) % services.size());
  }

  public ServiceDTO getConfigService(Env env) throws ServiceException {
    List<ServiceDTO> services = getServices(env, "config");
    if (services == null || services.size() == 0) {
      throw new ServiceException("No available config service");
    }
    return services.get(Math.abs(configCallCounts.getAndIncrement()) % services.size());
  }

  private List<ServiceDTO> getServices(Env env, String serviceUrl) {
    String domainName = MetaDomainConsts.getDomain(env);
    String url = domainName + "/services/" + serviceUrl;
    List<ServiceDTO> serviceDtos = null;
    try {
      ServiceDTO[] services = restTemplate.getForObject(new URI(url), ServiceDTO[].class);
      if (services != null && services.length > 0) {
        if (!serviceCaches.containsKey(env)) {
          serviceDtos = new ArrayList<ServiceDTO>();
          serviceCaches.put(env, serviceDtos);
        } else {
          serviceDtos = serviceCaches.get(env);
          serviceDtos.clear();
        }
        for (ServiceDTO service : services) {
          serviceDtos.add(service);
        }
      }
    } catch (Exception ex) {
      logger.warn(ex.getMessage());
    }
    return serviceDtos;
  }

  @PostConstruct
  private void postConstruct() {
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
  }
}
