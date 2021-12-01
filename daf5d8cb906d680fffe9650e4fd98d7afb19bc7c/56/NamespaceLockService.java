package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.core.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceLockService {

  @Autowired
  private AdminServiceAPI.NamespaceLockAPI namespaceLockAPI;

  public NamespaceLockDTO getNamespaceLock(String appId, Env env, String clusterName, String namespaceName){
    return namespaceLockAPI.getNamespaceLockOwner(appId, env, clusterName, namespaceName);

  }

}
