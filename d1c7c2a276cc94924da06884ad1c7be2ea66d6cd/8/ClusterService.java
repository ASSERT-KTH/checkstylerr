package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.portal.api.AdminServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusterService {

  @Autowired
  private AdminServiceAPI.ClusterAPI clusterAPI;

  public List<ClusterDTO> findClusters(Apollo.Env env, String appId){
    return clusterAPI.findClustersByApp(appId, env);
  }

}
