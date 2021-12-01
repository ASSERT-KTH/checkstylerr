package com.ctrip.framework.apollo.biz.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.repository.ClusterRepository;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.exception.ServiceException;

import com.google.common.base.Strings;

@Service
public class ClusterService {

  @Autowired
  private ClusterRepository clusterRepository;
  @Autowired
  private AuditService auditService;
  @Autowired
  private NamespaceService namespaceService;
  @Autowired
  private AppNamespaceService appNamespaceService;


  public boolean isClusterNameUnique(String appId, String clusterName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(clusterName, "ClusterName must not be null");
    return Objects.isNull(clusterRepository.findByAppIdAndName(appId, clusterName));
  }

  public Cluster findOne(String appId, String name) {
    return clusterRepository.findByAppIdAndName(appId, name);
  }

  public List<Cluster> findClusters(String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      return Collections.emptyList();
    }

    List<Cluster> clusters = clusterRepository.findByAppId(appId);
    Collections.sort(clusters);
    if (clusters == null) {
      return Collections.emptyList();
    }
    return clusters;
  }

  @Transactional
  public Cluster save(Cluster entity) {
    if (!isClusterNameUnique(entity.getAppId(), entity.getName())) {
      throw new ServiceException("cluster not unique");
    }
    entity.setId(0);//protection
    Cluster cluster = clusterRepository.save(entity);

    namespaceService.createPrivateNamespace(cluster.getAppId(), cluster.getName(), cluster.getDataChangeCreatedBy());

    auditService.audit(Cluster.class.getSimpleName(), cluster.getId(), Audit.OP.INSERT,
                       cluster.getDataChangeCreatedBy());

    return cluster;
  }

  @Transactional
  public void delete(long id, String operator) {
    Cluster cluster = clusterRepository.findOne(id);
    if (cluster == null) {
      return;
    }

    cluster.setDeleted(true);
    cluster.setDataChangeLastModifiedBy(operator);
    clusterRepository.save(cluster);

    auditService.audit(Cluster.class.getSimpleName(), id, Audit.OP.DELETE, operator);
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

  @Transactional
  public void createDefaultCluster(String appId, String createBy) {
    if (!isClusterNameUnique(appId, ConfigConsts.CLUSTER_NAME_DEFAULT)) {
      throw new ServiceException("cluster not unique");
    }
    Cluster cluster = new Cluster();
    cluster.setName(ConfigConsts.CLUSTER_NAME_DEFAULT);
    cluster.setAppId(appId);
    cluster.setDataChangeCreatedBy(createBy);
    cluster.setDataChangeLastModifiedBy(createBy);
    clusterRepository.save(cluster);

    auditService.audit(Cluster.class.getSimpleName(), cluster.getId(), Audit.OP.INSERT, createBy);
  }
}
