package com.ctrip.apollo.portal.controller;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.portal.constants.PortalConstants;
import com.ctrip.apollo.portal.entity.AppConfigVO;
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
  public AppConfigVO detail(@PathVariable long appId, @PathVariable String env,
                            @PathVariable long versionId) {

    if (appId <= 0) {
      throw new NotFoundException();
    }

    if (versionId == PortalConstants.LASTEST_VERSION_ID) {
      return configService.loadLatestConfig(Apollo.Env.DEV, appId);
    } else if (versionId > 0) {
      return configService.loadReleaseConfig(Apollo.Env.DEV, appId, versionId);
    } else {
      throw new NotFoundException();
    }
  }
}
