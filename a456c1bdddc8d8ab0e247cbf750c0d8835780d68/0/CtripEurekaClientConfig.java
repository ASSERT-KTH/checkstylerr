package com.ctrip.apollo.biz.eureka;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.stereotype.Component;

@Component
public class CtripEurekaClientConfig extends EurekaClientConfigBean {

  @Autowired
  private CtripEurekaSettings eurekaSettings;

  /**
   * Assert only one zone: defaultZone, but multiple environments.
   */
  public List<String> getEurekaServerServiceUrls(String myZone) {
    String serviceUrls = eurekaSettings.getDefaultEurekaUrl(myZone);
    if (serviceUrls != null) {
      return Arrays.asList(serviceUrls.split(","));
    }else{
      return super.getEurekaServerServiceUrls(myZone);
    }
  }

}
