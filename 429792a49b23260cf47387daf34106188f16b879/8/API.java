package com.ctrip.apollo.portal.api;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.ServiceException;
import com.ctrip.apollo.portal.service.ServiceLocator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class API {

  @Autowired
  protected ServiceLocator serviceLocator;

  @Autowired
  protected RestTemplate restTemplate;

  public String getAdminServiceHost(Env env) {
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
