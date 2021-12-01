package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.constant.CatEventType;
import com.dianping.cat.Cat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusterService {

  @Autowired
  private AdminServiceAPI.ClusterAPI clusterAPI;

  public List<ClusterDTO> findClusters(Env env, String appId){
    return clusterAPI.findClustersByApp(appId, env);
  }

  public ClusterDTO createCluster(Env env, ClusterDTO cluster){
    if (!clusterAPI.isClusterUnique(cluster.getAppId(), env, cluster.getName())){
      throw new BadRequestException(String.format("cluster %s already exists.", cluster.getName()));
    }
    ClusterDTO clusterDTO = clusterAPI.create(env, cluster);

    Cat.logEvent(CatEventType.CREATE_CLUSTER, cluster.getAppId(), "0", cluster.getName());

    return clusterDTO;
  }

}
