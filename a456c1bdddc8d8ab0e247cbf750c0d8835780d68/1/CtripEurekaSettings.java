package com.ctrip.apollo.biz.eureka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.enums.EnvUtils;
import com.ctrip.framework.foundation.Foundation;

@Component
public class CtripEurekaSettings {

  @Value("${ctrip.eureka.dev:http://localhost:8080/eureka}")
  private String devEureka;

  @Value("${ctrip.eureka.fat:http://localhost:8080/eureka}")
  private String fatEureka;

  @Value("${ctrip.eureka.uat:http://localhost:8080/eureka}")
  private String uatEureka;

  @Value("${ctrip.eureka.pro:http://localhost:8080/eureka}")
  private String proEureka;

  public String getDefaultEurekaUrl(String zone) {
    Env env = EnvUtils.transformEnv(Foundation.server().getEnvType());
    if (env == null) {
      return null;
    }
    switch (env) {
      case LOCAL:
        return null;
      case DEV:
        return devEureka;
      case FAT:
      case FWS:
        return fatEureka;
      case UAT:
        return uatEureka;
      case TOOLS:
      case PRO:
        return proEureka;
      default:
        return null;
    }
  }
}
