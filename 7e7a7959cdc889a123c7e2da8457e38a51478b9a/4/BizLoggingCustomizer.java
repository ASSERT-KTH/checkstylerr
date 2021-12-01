package com.ctrip.framework.apollo.portal.cumsomize;

import com.ctrip.framework.apollo.common.customize.LoggingCustomizer;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ctrip")
public class BizLoggingCustomizer extends LoggingCustomizer{

  private static final String CLOGGING_SERVER_URL_KEY = "clogging.server.url";
  private static final String CLOGGING_SERVER_PORT_KEY = "clogging.server.port";

  @Autowired
  private ServerConfigRepository serverConfigRepository;

  private String cloggingUrl;
  private String cloggingPort;

  @Override
  protected String cloggingUrl() {
    if (cloggingUrl == null){
      cloggingUrl = serverConfigRepository.findByKey(CLOGGING_SERVER_URL_KEY).getValue();
    }
    return cloggingUrl;
  }

  @Override
  protected String cloggingPort() {
    if (cloggingPort == null){
      cloggingPort = serverConfigRepository.findByKey(CLOGGING_SERVER_PORT_KEY).getValue();
    }
    return cloggingPort;
  }
}
