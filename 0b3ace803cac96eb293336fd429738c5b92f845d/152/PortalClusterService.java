package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.ClusterDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalClusterService {

  @Autowired
  private AdminServiceAPI.ClusterAPI clusterAPI;

  public List<ClusterDTO> findClusters(Env env, String appId){
    return clusterAPI.findClustersByApp(appId, env);
  }

}
