package com.ctrip.framework.apollo.common.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.foundation.Foundation;

@RestController
@RequestMapping(path = "/apollo")
public class ApolloInfoController {

  @RequestMapping("app")
  public String getApp() {
    return Foundation.app().toString();
  }

  @RequestMapping("web")
  public String getEnv() {
    return Foundation.web().toString();
  }

  @RequestMapping("net")
  public String getNet() {
    return Foundation.net().toString();
  }

  @RequestMapping("server")
  public String getServer() {
    return Foundation.server().toString();
  }

  @RequestMapping("version")
  public String getVersion() {
    return Apollo.VERSION;
  }
}
