package com.ctrip.apollo.adminservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.service.AppNamespaceService;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.dto.AppNamespaceDTO;

import java.util.List;

@RestController
public class AppNamespaceController {

  @Autowired
  private AppNamespaceService appNamespaceService;

  @RequestMapping("/apps/{appId}/appnamespace/{appnamespace}/unique")
  public boolean isAppNamespaceUnique(@PathVariable("appId") String appId,
      @PathVariable("appnamespace") String appnamespace) {
    return appNamespaceService.isAppNamespaceNameUnique(appId, appnamespace);
  }

  @RequestMapping("/appnamespaces/public")
  public List<AppNamespaceDTO> findPublicAppNamespaces(){
    List<AppNamespace> appNamespaces = appNamespaceService.findPublicAppNamespaces();
    return BeanUtils.batchTransform(AppNamespaceDTO.class, appNamespaces);
  }

}
