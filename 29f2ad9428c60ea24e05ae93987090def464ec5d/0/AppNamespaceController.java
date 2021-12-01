package com.ctrip.framework.apollo.adminservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import java.util.List;

@RestController
public class AppNamespaceController {

  @Autowired
  private AppNamespaceService appNamespaceService;

  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public AppNamespaceDTO create(@RequestBody AppNamespaceDTO appNamespace) {

    AppNamespace entity = BeanUtils.transfrom(AppNamespace.class, appNamespace);
    AppNamespace managedEntity = appNamespaceService.findOne(entity.getAppId(), entity.getName());

    if (managedEntity != null) {
      throw new BadRequestException("app namespaces already exist.");
    }

    if (StringUtils.isEmpty(appNamespace.getFormat())){
      appNamespace.setFormat(ConfigFileFormat.Properties.getValue());
    }

    entity = appNamespaceService.createAppNamespace(entity);

    return BeanUtils.transfrom(AppNamespaceDTO.class, entity);

  }

}
