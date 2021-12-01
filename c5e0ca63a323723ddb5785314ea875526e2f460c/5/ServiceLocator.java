package com.ctrip.framework.apollo.portal.service;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.exception.ServiceException;

/**
 * @author liuym
 */
@Service
public class ServiceLocator {

  private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);

  private static final int DEFAULT_TIMEOUT_MS = 1000;

  private static final int RETRY_TIMES = 3;

  private static final int CALL_META_SERVER_THRESHOLD = 10;

  private static final String ADMIN_SERVICE_URL_PATH = "/services/admin";

  private RestTemplate restTemplate;

  @Autowired
  private HttpMessageConverters httpMessageConverters;

  private Map<Env, ServiceDTO[]> serviceAddressCache = new ConcurrentHashMap<>();

  private final AtomicInteger adminCallCounts = new AtomicInteger(0);

  @PostConstruct
  private void postConstruct() {
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
  }


  public ServiceDTO getServiceAddress(Env env) throws ServiceException {

    if (adminCallCounts.get() % CALL_META_SERVER_THRESHOLD == 0) {
      return getServiceAddressFromMetaServer(env);
    } else {
      //if cached then return from cache
      ServiceDTO[] serviceDTOs = serviceAddressCache.get(env);
      if (serviceDTOs != null && serviceDTOs.length > 0){
        return randomServiceAddress(serviceDTOs);
      }else {//return from meta server
        return getServiceAddressFromMetaServer(env);
      }
    }


  }

  public ServiceDTO getServiceAddressFromMetaServer(Env env) {
    //retry
    for (int i = 0; i < RETRY_TIMES; i++) {
      ServiceDTO[] services = getServices(env);
      if (services != null && services.length > 0) {
        serviceAddressCache.put(env, services);
        return randomServiceAddress(services);
      } else {
        logger.warn(String.format("can not get %s admin service address at %d time", env, i));
      }
    }
    logger.error(String.format("can not get %s admin service address", env));
    throw new ServiceException("No available admin service");
  }


  private ServiceDTO[] getServices(Env env) {
    String domainName = MetaDomainConsts.getDomain(env);
    String url = domainName + ADMIN_SERVICE_URL_PATH;

    try {
      return restTemplate.getForObject(new URI(url), ServiceDTO[].class);
    } catch (Exception ex) {
      logger.warn(ex.getMessage());
      return null;
    }
  }

  private ServiceDTO randomServiceAddress(ServiceDTO[] services){
    return services[Math.abs(adminCallCounts.getAndIncrement()) % services.length];
  }

}
