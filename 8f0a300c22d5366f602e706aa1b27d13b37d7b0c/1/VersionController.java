package com.ctrip.apollo.portal.controller;

import com.google.common.base.Strings;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.VersionDTO;
import com.ctrip.apollo.portal.service.VersionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/version")
public class VersionController {

  @Autowired
  private VersionService versionService;

  @RequestMapping("/{appId}/{env}")
  public List<VersionDTO> versions(@PathVariable String appId, @PathVariable String env) {

    if (Strings.isNullOrEmpty(appId) || Strings.isNullOrEmpty(env)) {
      throw new IllegalArgumentException(
          String.format("app id and env can not be empty. app id:%s , env:%s", appId, env));
    }

    return versionService.findVersionsByApp(Apollo.Env.valueOf(env), appId);
  }
}
