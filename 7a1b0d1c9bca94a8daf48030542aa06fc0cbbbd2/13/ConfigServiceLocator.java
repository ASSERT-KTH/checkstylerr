package com.ctrip.apollo.internals;

import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.env.ClientEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ConfigServiceLocator {

  private static final Logger logger = LoggerFactory.getLogger(ConfigServiceLocator.class);

  private RestTemplate restTemplate = new RestTemplate();

  private List<ServiceDTO> serviceCaches = new ArrayList<>();

  public List<ServiceDTO> getConfigServices() {
    ClientEnvironment env = ClientEnvironment.getInstance();
    String domainName = env.getMetaServerDomainName();
    String url = domainName + "/services/config";
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
