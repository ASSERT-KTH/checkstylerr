package com.ctrip.framework.apollo.biz.customize;

import com.ctrip.framework.apollo.biz.service.ServerConfigService;
import com.ctrip.framework.apollo.common.customize.LoggingCustomizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ctrip")
public class BizLoggingCustomizer extends LoggingCustomizer{

  private static final String CLOGGING_SERVER_URL_KEY = "clogging.server.url";
  private static final String CLOGGING_SERVER_PORT_KEY = "clogging.server.port";

  @Autowired
  private ServerConfigService serverConfigService;

  private String cloggingUrl;
  private String cloggingPort;

  @Override
  protected String cloggingUrl() {
    if (cloggingUrl == null){
      cloggingUrl = serverConfigService.getValue(CLOGGING_SERVER_URL_KEY);
    }
    return cloggingUrl;
  }

  @Override
  protected String cloggingPort() {
    if (cloggingPort == null){
      cloggingPort = serverConfigService.getValue(CLOGGING_SERVER_PORT_KEY);
    }
    return cloggingPort;
  }
}
