package com.ctrip.apollo.portal.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.core.exception.ServiceException;

/**
 * @author liuym
 */
@Service
public class ServiceLocator {

  private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);

  private RestTemplate restTemplate = new RestTemplate();

  private List<ServiceDTO> serviceCaches = new ArrayList<>();

  public ServiceDTO getAdminService(Env env) throws ServiceException {
    List<ServiceDTO> services = getServices(env, "admin");
    if (services.size() == 0) {
      throw new ServiceException("No available admin service");
    }
    return services.get(0);
  }

  public ServiceDTO getConfigService(Env env) throws ServiceException {
    List<ServiceDTO> services = getServices(env, "config");
    if (services.size() == 0) {
      throw new ServiceException("No available config service");
    }
    return services.get(0);
  }

  private List<ServiceDTO> getServices(Env env, String serviceUrl) {
    String domainName = MetaDomainConsts.getDomain(env);
    String url = domainName + "/services/" + serviceUrl;
    try {
      ServiceDTO[] services = restTemplate.getForObject(new URI(url), ServiceDTO[].class);
      if (services != null && services.length > 0) {
        serviceCaches.clear();
        for (ServiceDTO service : services) {
          serviceCaches.add(service);
        }
      }
    } catch (Exception ex) {
      logger.warn(ex.getMessage());
    }
    return serviceCaches;
  }
}
