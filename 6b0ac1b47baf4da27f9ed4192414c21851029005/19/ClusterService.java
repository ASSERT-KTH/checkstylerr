package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.common.utils.BeanUtils;

@Service
public class ClusterService {

  @Autowired
  private ClusterRepository clusterRepository;

  @Autowired
  private AuditService auditService;

  public Cluster findOne(String appId, String name) {
    return clusterRepository.findByAppIdAndName(appId, name);
  }

  @Transactional
  public Cluster save(Cluster entity) {
    Cluster cluster = clusterRepository.save(entity);

    auditService.audit(Cluster.class.getSimpleName(), cluster.getId(), Audit.OP.INSERT,
        cluster.getDataChangeCreatedBy());

    return cluster;
  }

  @Transactional
  public void delete(long id, String owner) {
    clusterRepository.delete(id);

    auditService.audit(Cluster.class.getSimpleName(), id, Audit.OP.DELETE, owner);
  }

  @Transactional
  public Cluster update(Cluster cluster) {
    Cluster managedCluster =
        clusterRepository.findByAppIdAndName(cluster.getAppId(), cluster.getName());
    BeanUtils.copyEntityProperties(cluster, managedCluster);
    managedCluster = clusterRepository.save(managedCluster);

    auditService.audit(Cluster.class.getSimpleName(), managedCluster.getId(), Audit.OP.UPDATE,
        managedCluster.getDataChangeLastModifiedBy());

    return managedCluster;
  }
}
