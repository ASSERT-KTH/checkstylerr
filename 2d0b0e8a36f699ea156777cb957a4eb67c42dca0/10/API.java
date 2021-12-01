package com.ctrip.apollo.portal.api;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.ctrip.apollo.common.auth.RestTemplateFactory;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.ServiceException;
import com.ctrip.apollo.portal.service.ServiceLocator;

public class API {

  @Autowired
  protected ServiceLocator serviceLocator;

  @Autowired
  private RestTemplateFactory restTemplateFactory;

  protected RestTemplate restTemplate;

  @PostConstruct
  private void postConstruct() {
    restTemplate = restTemplateFactory.getObject();
  }

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
