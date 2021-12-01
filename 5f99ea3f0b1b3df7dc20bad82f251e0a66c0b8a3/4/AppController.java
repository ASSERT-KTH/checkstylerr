package com.ctrip.apollo.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.ClusterNavTree;
import com.ctrip.apollo.portal.service.AppService;
import com.google.common.base.Strings;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;

  @RequestMapping("/{appId}/navtree")
  public ClusterNavTree nav(@PathVariable String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      throw new BadRequestException("app id can not be empty.");
    }

    return appService.buildClusterNavTree(appId);
  }

  @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"})
  public AppDTO create(@RequestBody AppDTO app) {
    if (isInvalidApp(app)){
      throw new BadRequestException("request payload contains empty");
    }
    AppDTO createdApp = appService.save(app);
    return createdApp;
  }

  private boolean isInvalidApp(AppDTO app) {
    return StringUtils.isContainEmpty(app.getName(), app.getAppId(), app.getOwnerEmail(), app.getOwnerName());
  }
}

