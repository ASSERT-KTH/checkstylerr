package com.ctrip.apollo.adminservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.service.AppNamespaceService;

@RestController
public class AppNamespaceController {

  @Autowired
  private AppNamespaceService appNamespaceService;

  @RequestMapping("/apps/{appId}/appnamespace/{appnamespace}/unique")
  public boolean isAppNamespaceUnique(@PathVariable("appId") String appId,
      @PathVariable("appnamespace") String appnamespace) {
    return appNamespaceService.isAppNamespaceNameUnique(appId, appnamespace);
  }
}
