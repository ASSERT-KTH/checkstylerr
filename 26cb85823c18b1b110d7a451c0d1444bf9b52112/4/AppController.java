package com.ctrip.apollo.portal.controller;

import com.google.common.base.Strings;

import com.ctrip.apollo.portal.entity.ClusterNavTree;
import com.ctrip.apollo.portal.service.AppService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;

  @RequestMapping("/{appId}/navtree")
  public ClusterNavTree nav(@PathVariable String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      throw new IllegalArgumentException("app id can not be empty.");
    }

    return appService.buildClusterNavTree(appId);
  }

}

