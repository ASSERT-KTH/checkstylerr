package com.ctrip.framework.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.core.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.exception.NotFoundException;

@RestController
public class ClusterController {

  @Autowired
  private ClusterService clusterService;

  @RequestMapping(path = "/apps/{appId}/clusters", method = RequestMethod.POST)
  public ClusterDTO create(@PathVariable("appId") String appId, @RequestBody ClusterDTO dto) {
    if (!InputValidator.isValidClusterNamespace(dto.getName())) {
      throw new BadRequestException(String.format("Cluster格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
    }
    Cluster entity = BeanUtils.transfrom(Cluster.class, dto);
    Cluster managedEntity = clusterService.findOne(appId, entity.getName());
    if (managedEntity != null) {
      throw new BadRequestException("cluster already exist.");
    }
    entity = clusterService.save(entity);

    dto = BeanUtils.transfrom(ClusterDTO.class, entity);
    return dto;
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName:.+}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId,
                     @PathVariable("clusterName") String clusterName, @RequestParam String operator) {
    Cluster entity = clusterService.findOne(appId, clusterName);
    if (entity == null) {
      throw new NotFoundException("cluster not found for clusterName " + clusterName);
    }
    clusterService.delete(entity.getId(), operator);
  }

  @RequestMapping("/apps/{appId}/clusters")
  public List<ClusterDTO> find(@PathVariable("appId") String appId) {
    List<Cluster> clusters = clusterService.findClusters(appId);
    return BeanUtils.batchTransform(ClusterDTO.class, clusters);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName:.+}")
  public ClusterDTO get(@PathVariable("appId") String appId,
                        @PathVariable("clusterName") String clusterName) {
    Cluster cluster = clusterService.findOne(appId, clusterName);
    if (cluster == null) {
      throw new NotFoundException("cluster not found for name " + clusterName);
    }
    return BeanUtils.transfrom(ClusterDTO.class, cluster);
  }

  @RequestMapping("/apps/{appId}/cluster/{clusterName}/unique")
  public boolean isAppIdUnique(@PathVariable("appId") String appId,
                               @PathVariable("clusterName") String clusterName) {
    return clusterService.isClusterNameUnique(appId, clusterName);
  }
}
