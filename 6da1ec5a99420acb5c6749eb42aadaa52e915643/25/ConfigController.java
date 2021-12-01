package com.ctrip.apollo.portal.controller;

import com.google.common.base.Strings;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.AppConfigVO;
import com.ctrip.apollo.portal.constants.PortalConstants;
import com.ctrip.apollo.portal.exception.NotFoundException;
import com.ctrip.apollo.portal.service.ConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configs")
public class ConfigController {

  @Autowired
  private ConfigService configService;

  @RequestMapping("/{appId}/{env}/{versionId}")
  public AppConfigVO detail(@PathVariable String appId, @PathVariable String env,
                            @PathVariable long versionId) {

    if (Strings.isNullOrEmpty(appId) || Strings.isNullOrEmpty(env)) {
      throw new IllegalArgumentException(
          String.format("app id and env can not be empty. app id:%s , env:%s", appId, env));
    }

    Apollo.Env e = Apollo.Env.valueOf(env);

    if (versionId == PortalConstants.LASTEST_VERSION_ID) {

      return configService.loadLatestConfig(e, appId);

//    } else if (versionId > 0) {
//
//      return configService.loadReleaseConfig(e, appId, versionId);
//
    }
    else {
      throw new NotFoundException();
    }
  }
}
