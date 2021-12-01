package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.ClusterDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusterService {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private AdminServiceAPI.ClusterAPI clusterAPI;

  public List<ClusterDTO> findClusters(Env env, String appId){
    return clusterAPI.findClustersByApp(appId, env);
  }

  public ClusterDTO createCluster(Env env, ClusterDTO cluster){
    String operator = userInfoHolder.getUser().getUserId();
    cluster.setDataChangeLastModifiedBy(operator);
    cluster.setDataChangeCreatedBy(operator);
    return clusterAPI.createOrUpdate(env, cluster);
  }

}
