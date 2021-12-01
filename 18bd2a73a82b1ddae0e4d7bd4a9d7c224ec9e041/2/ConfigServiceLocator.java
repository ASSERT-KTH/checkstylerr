package com.ctrip.apollo.internals;

import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.env.ClientEnvironment;
import com.ctrip.apollo.util.http.HttpRequest;
import com.ctrip.apollo.util.http.HttpResponse;
import com.ctrip.apollo.util.http.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.List;

@Named(type = ConfigServiceLocator.class)
public class ConfigServiceLocator {
  private static final Logger logger = LoggerFactory.getLogger(ConfigServiceLocator.class);
  @Inject
  private HttpUtil m_httpUtil;
  private List<ServiceDTO> serviceCaches = new ArrayList<>();

  public List<ServiceDTO> getConfigServices() {
    ClientEnvironment env = ClientEnvironment.getInstance();
    String domainName = env.getMetaServerDomainName();
    String url = domainName + "/services/config";

    HttpRequest request = new HttpRequest(url);

    try {
      HttpResponse<ServiceDTO[]> response = m_httpUtil.doGet(request, ServiceDTO[].class);
      ServiceDTO[] services = response.getBody();
      if (services != null && services.length > 0) {
        serviceCaches.clear();
        for (ServiceDTO service : services) {
          serviceCaches.add(service);
        }
      }
    } catch (Throwable t) {
      logger.error("Get config services failed", t);
      throw new RuntimeException("Get config services failed", t);
    }

    return serviceCaches;
  }
}
