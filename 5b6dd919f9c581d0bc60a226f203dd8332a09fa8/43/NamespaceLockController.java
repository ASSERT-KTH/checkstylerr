package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.service.NamespaceLockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NamespaceLockController {

  @Autowired
  private NamespaceLockService namespaceLockService;

  @RequestMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock")
  public NamespaceLockDTO getNamespaceLock(@PathVariable String appId, @PathVariable String env,
                                           @PathVariable String clusterName, @PathVariable String namespaceName) {

    return namespaceLockService.getNamespaceLock(appId, Env.valueOf(env), clusterName, namespaceName);
  }

}
