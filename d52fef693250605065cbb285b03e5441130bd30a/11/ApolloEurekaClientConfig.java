package com.ctrip.framework.apollo.biz.eureka;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import com.ctrip.framework.apollo.biz.service.ServerConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class ApolloEurekaClientConfig extends EurekaClientConfigBean {
  static final String EUREKA_URL_CONFIG = "eureka.service.url";
  private static final Splitter URL_SPLITTER = Splitter.on(",").omitEmptyStrings();

  @Autowired
  private ServerConfigService serverConfigService;

  @Autowired
  private Environment environment;

  /**
   * Assert only one zone: defaultZone, but multiple environments.
   */
  public List<String> getEurekaServerServiceUrls(String myZone) {
    //First check if there is any system property override
    if (!Strings.isNullOrEmpty(environment.getProperty(EUREKA_URL_CONFIG))) {
      return URL_SPLITTER.splitToList(environment.getProperty(EUREKA_URL_CONFIG));
    }

    //Second check if it is configured in database
    String eurekaUrl = serverConfigService.getValue(EUREKA_URL_CONFIG);

    if (!Strings.isNullOrEmpty(eurekaUrl)) {
      return URL_SPLITTER.splitToList(eurekaUrl);

    }

    //fallback to default
    return super.getEurekaServerServiceUrls(myZone);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
