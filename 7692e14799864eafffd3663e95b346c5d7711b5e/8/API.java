package com.ctrip.apollo.portal.api;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.exception.ServiceException;
import com.ctrip.apollo.portal.service.ServiceLocator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class API {

  @Autowired
  protected ServiceLocator serviceLocator;

  protected RestTemplate restTemplate = new RestTemplate();

  public String getAdminServiceHost(Apollo.Env env) {
    // 本地测试用
    // return "http://localhost:8090";
    try {
      return serviceLocator.getAdminService(env).getHomepageUrl();
    } catch (ServiceException e) {
      e.printStackTrace();
    }
    return "";
  }
}
