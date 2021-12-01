package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.exception.ServiceException;
import com.ctrip.framework.apollo.portal.service.NamespaceLockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class NamespaceLockController {

  @Autowired
  private NamespaceLockService namespaceLockService;

  @RequestMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock")
  public NamespaceLockDTO getNamespaceLock(@PathVariable String appId, @PathVariable String env,
                                           @PathVariable String clusterName, @PathVariable String namespaceName){

    try {
      return namespaceLockService.getNamespaceLock(appId, Env.valueOf(env), clusterName, namespaceName);
    } catch (HttpClientErrorException e){
      if (e.getStatusCode() == HttpStatus.NOT_FOUND){
        return null;
      }
      throw new ServiceException("service error", e);
    }

  }

}
