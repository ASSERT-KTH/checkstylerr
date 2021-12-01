package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.service.ClusterService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.common.controller.ActiveUser;
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
  public ResponseEntity<ClusterDTO> create(@PathVariable("appId") String appId,
      @RequestBody ClusterDTO dto, @ActiveUser UserDetails user) {
    Cluster entity = BeanUtils.transfrom(Cluster.class, dto);
    entity.setDataChangeCreatedBy(user.getUsername());
    entity = clusterService.save(entity);
    dto = BeanUtils.transfrom(ClusterDTO.class, entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}", method = RequestMethod.PUT)
  public ClusterDTO update(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName, @RequestBody ClusterDTO dto,
      @ActiveUser UserDetails user) {
    if (!clusterName.equals(dto.getName())) {
      throw new IllegalArgumentException(String
          .format("Path variable %s is not equals to object field %s", clusterName, dto.getName()));
    }
    Cluster entity = clusterService.findOne(appId, clusterName);
    if (entity == null) throw new NotFoundException("cluster not found for name " + clusterName);
    entity.setDataChangeLastModifiedBy(user.getUsername());
    entity = clusterService.update(BeanUtils.transfrom(Cluster.class, dto));
    return BeanUtils.transfrom(ClusterDTO.class, entity);
  }

}
