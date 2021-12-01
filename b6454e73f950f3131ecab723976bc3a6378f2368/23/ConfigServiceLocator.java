package com.ctrip.apollo.client.loader;

import com.ctrip.apollo.client.env.ClientEnvironment;
import com.ctrip.apollo.core.serivce.ApolloService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ConfigServiceLocator {

  private static final Logger logger = LoggerFactory.getLogger(ConfigServiceLocator.class);

  private RestTemplate restTemplate = new RestTemplate();

  private List<ApolloService> serviceCaches = new ArrayList<>();

  public List<ApolloService> getConfigServices() {
    ClientEnvironment env = ClientEnvironment.getInstance();
    String domainName = env.getMetaServerDomainName();
    String url = domainName + "/services/config";
    try {
      ApolloService[] services = restTemplate.getForObject(new URI(url), ApolloService[].class);
      if (services != null && services.length > 0) {
        serviceCaches.clear();
        for (ApolloService service : services) {
          serviceCaches.add(service);
        }
      }
    } catch (Exception ex) {
      logger.warn(ex.getMessage());
    }
    return serviceCaches;
  }
}
