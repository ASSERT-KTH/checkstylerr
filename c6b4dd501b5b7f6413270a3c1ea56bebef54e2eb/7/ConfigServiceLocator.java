package com.ctrip.apollo.internals;

import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.util.ConfigUtil;
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
  @Inject
  private ConfigUtil m_configUtil;
  private List<ServiceDTO> serviceCaches = new ArrayList<>();

  /**
   * Get the config service info from remote meta server.
   * @return the services dto
   */
  public List<ServiceDTO> getConfigServices() {
    String domainName = m_configUtil.getMetaServerDomainName();
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
    } catch (Throwable ex) {
      logger.error("Get config services failed", ex);
      throw new RuntimeException("Get config services failed", ex);
    }

    return serviceCaches;
  }
}
