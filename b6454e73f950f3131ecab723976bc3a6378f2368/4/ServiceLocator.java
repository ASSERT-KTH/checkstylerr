package com.ctrip.apollo.portal.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.serivce.ApolloService;

/**
 * @author liuym
 */
@Service
public class ServiceLocator {

  private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);

  private RestTemplate restTemplate = new RestTemplate();

  private List<ApolloService> serviceCaches = new ArrayList<>();

  public List<ApolloService> getAdminServices(Env env) {
    return getServices(env, "admin");
  }

  public String getAdminService(Env env) {
  //本地测试用
//    return "http://localhost:8090";
    List<ApolloService> services = getAdminServices(env);
    if (services.size() == 0) {
      throw new RuntimeException("No available admin service");
    }
    return services.get(0).getHomepageUrl();
  }

  public List<ApolloService> getConfigServices(Env env) {
    return getServices(env, "config");
  }

  private List<ApolloService> getServices(Env env, String serviceUrl) {
    String domainName = MetaDomainConsts.getDomain(env);
    String url = domainName + "/services/" + serviceUrl;
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
