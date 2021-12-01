package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.service.ClusterService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.common.auth.ActiveUser;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.exception.NotFoundException;

@RestController
public class ClusterController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private ClusterService clusterService;

  @RequestMapping(path = "/apps/{appId}/clusters", method = RequestMethod.POST)
  public ClusterDTO createOrUpdate(@PathVariable("appId") String appId, @RequestBody ClusterDTO dto,
      @ActiveUser UserDetails user) {
    Cluster entity = BeanUtils.transfrom(Cluster.class, dto);
    Cluster managedEntity = clusterService.findOne(appId, entity.getName());
    if (managedEntity != null) {
      managedEntity.setDataChangeLastModifiedBy(user.getUsername());
      BeanUtils.copyEntityProperties(entity, managedEntity);
      entity = clusterService.update(managedEntity);
    } else {
      entity.setDataChangeCreatedBy(user.getUsername());
      entity = clusterService.save(entity);
    }

    dto = BeanUtils.transfrom(ClusterDTO.class, entity);
    return dto;
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName, @ActiveUser UserDetails user) {
    Cluster entity = clusterService.findOne(appId, clusterName);
    if (entity == null)
      throw new NotFoundException("cluster not found for clusterName " + clusterName);
    clusterService.delete(entity.getId(), user.getUsername());
  }

  @RequestMapping("/apps/{appId}/clusters")
  public List<ClusterDTO> find(@PathVariable("appId") String appId) {
    List<Cluster> clusters = viewService.findClusters(appId);
    return BeanUtils.batchTransform(ClusterDTO.class, clusters);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}")
  public ClusterDTO get(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    Cluster cluster = clusterService.findOne(appId, clusterName);
    if (cluster == null) throw new NotFoundException("cluster not found for name " + clusterName);
    return BeanUtils.transfrom(ClusterDTO.class, cluster);
  }

}
