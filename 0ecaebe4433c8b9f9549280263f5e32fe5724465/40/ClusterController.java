package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.service.ClusterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkArgument;
import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class ClusterController {

  @Autowired
  private ClusterService clusterService;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @PreAuthorize(value = "@permissionValidator.hasCreateClusterPermission(#appId)")
  @RequestMapping(value = "apps/{appId}/envs/{env}/clusters", method = RequestMethod.POST)
  public ClusterDTO createCluster(@PathVariable String appId, @PathVariable String env,
                                  @RequestBody ClusterDTO cluster){

    checkModel(cluster != null);
    checkArgument(cluster.getAppId(), cluster.getName());

    if (!InputValidator.isValidClusterNamespace(cluster.getName())) {
      throw new BadRequestException(String.format("Cluster格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }

    String operator = userInfoHolder.getUser().getUserId();
    cluster.setDataChangeLastModifiedBy(operator);
    cluster.setDataChangeCreatedBy(operator);

    return clusterService.createCluster(Env.valueOf(env), cluster);
  }

}
