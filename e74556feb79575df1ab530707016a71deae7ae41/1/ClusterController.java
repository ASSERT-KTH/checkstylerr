package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.service.ClusterService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ClusterDTO;

@RestController
public class ClusterController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private ClusterService clusterService;

  @RequestMapping("/apps/{appId}/clusters")
  public List<ClusterDTO> findClusters(@PathVariable("appId") String appId) {
    List<Cluster> clusters = viewService.findClusters(appId);
    return BeanUtils.batchTransform(ClusterDTO.class, clusters);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}")
  public ClusterDTO getCluster(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    Cluster cluster = clusterService.findOne(appId, clusterName);
    return BeanUtils.transfrom(ClusterDTO.class, cluster);
  }
}
