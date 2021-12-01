package com.ctrip.framework.apollo.biz.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.exception.ServiceException;

@Service
public class NamespaceService {

  @Autowired
  private NamespaceRepository namespaceRepository;
  @Autowired
  private AuditService auditService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private ItemService itemService;
  @Autowired
  private CommitService commitService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private ClusterService clusterService;
  @Autowired
  private NamespaceBranchService namespaceBranchService;
  @Autowired
  private ReleaseHistoryService releaseHistoryService;
  @Autowired
  private NamespaceLockService namespaceLockService;
  @Autowired
  private InstanceService instanceService;



  public Namespace findOne(Long namespaceId) {
    return namespaceRepository.findOne(namespaceId);
  }

  public Namespace findOne(String appId, String clusterName, String namespaceName) {
    return namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
                                                                         namespaceName);
  }

  public List<Namespace> findNamespaces(String appId, String clusterName) {
    List<Namespace> namespaces = namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(appId, clusterName);
    if (namespaces == null) {
      return Collections.emptyList();
    }
    return namespaces;
  }

  public List<Namespace> findByAppIdAndNamespaceName(String appId, String namespaceName) {
    return namespaceRepository.findByAppIdAndNamespaceName(appId, namespaceName);
  }

  public Namespace findChildNamespace(String appId, String parentClusterName, String namespaceName) {
    List<Namespace> namespaces = findByAppIdAndNamespaceName(appId, namespaceName);
    if (CollectionUtils.isEmpty(namespaces) || namespaces.size() == 1) {
      return null;
    }

    List<Cluster> childClusters = clusterService.findChildClusters(appId, parentClusterName);
    if (CollectionUtils.isEmpty(childClusters)) {
      return null;
    }

    Set<String> childClusterNames = childClusters.stream().map(Cluster::getName).collect(Collectors.toSet());
    //the child namespace is the intersection of the child clusters and child namespaces
    for (Namespace namespace : namespaces) {
      if (childClusterNames.contains(namespace.getClusterName())) {
        return namespace;
      }
    }

    return null;
  }

  public Namespace findChildNamespace(Namespace parentNamespace) {
    String appId = parentNamespace.getAppId();
    String parentClusterName = parentNamespace.getClusterName();
    String namespaceName = parentNamespace.getNamespaceName();

    return findChildNamespace(appId, parentClusterName, namespaceName);

  }

  public Namespace findParentNamespace(Namespace namespace) {
    String appId = namespace.getAppId();
    String namespaceName = namespace.getNamespaceName();

    Cluster cluster = clusterService.findOne(appId, namespace.getClusterName());
    if (cluster != null && cluster.getParentClusterId() > 0) {
      Cluster parentCluster = clusterService.findOne(cluster.getParentClusterId());
      return findOne(appId, parentCluster.getName(), namespaceName);
    }

    return null;
  }

  public boolean isChildNamespace(Namespace namespace) {
    return findParentNamespace(namespace) != null;
  }

  public boolean isNamespaceUnique(String appId, String cluster, String namespace) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(cluster, "Cluster must not be null");
    Objects.requireNonNull(namespace, "Namespace must not be null");
    return Objects.isNull(
        namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, cluster, namespace));
  }

  @Transactional
  public void deleteByAppIdAndClusterName(String appId, String clusterName, String operator) {

    List<Namespace> toDeleteNamespaces = findNamespaces(appId, clusterName);

    for (Namespace namespace : toDeleteNamespaces) {

      deleteNamespace(namespace, operator);

    }
  }

  @Transactional
  public Namespace deleteNamespace(Namespace namespace, String operator) {
    String appId = namespace.getAppId();
    String clusterName = namespace.getClusterName();
    String namespaceName = namespace.getNamespaceName();

    itemService.batchDelete(namespace.getId(), operator);
    commitService.batchDelete(appId, clusterName, namespace.getNamespaceName(), operator);

    if (!isChildNamespace(namespace)) {
      releaseService.batchDelete(appId, clusterName, namespace.getNamespaceName(), operator);
    }

    //delete child namespace
    Namespace childNamespace = findChildNamespace(namespace);
    if (childNamespace != null) {
      namespaceBranchService.deleteBranch(appId, clusterName, namespaceName,
                                          childNamespace.getClusterName(), NamespaceBranchStatus.DELETED, operator);
      //delete child namespace's releases. Notice: delete child namespace will not delete child namespace's releases
      releaseService.batchDelete(appId, childNamespace.getClusterName(), namespaceName, operator);
    }

    releaseHistoryService.batchDelete(appId, clusterName, namespaceName, operator);

    instanceService.batchDeleteInstanceConfig(appId, clusterName, namespaceName);

    namespaceLockService.unlock(namespace.getId());

    namespace.setDeleted(true);
    namespace.setDataChangeLastModifiedBy(operator);

    auditService.audit(Namespace.class.getSimpleName(), namespace.getId(), Audit.OP.DELETE, operator);

    return namespaceRepository.save(namespace);
  }

  @Transactional
  public Namespace save(Namespace entity) {
    if (!isNamespaceUnique(entity.getAppId(), entity.getClusterName(), entity.getNamespaceName())) {
      throw new ServiceException("namespace not unique");
    }
    entity.setId(0);//protection
    Namespace namespace = namespaceRepository.save(entity);

    auditService.audit(Namespace.class.getSimpleName(), namespace.getId(), Audit.OP.INSERT,
                       namespace.getDataChangeCreatedBy());

    return namespace;
  }

  @Transactional
  public Namespace update(Namespace namespace) {
    Namespace managedNamespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(
        namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());
    BeanUtils.copyEntityProperties(namespace, managedNamespace);
    managedNamespace = namespaceRepository.save(managedNamespace);

    auditService.audit(Namespace.class.getSimpleName(), managedNamespace.getId(), Audit.OP.UPDATE,
                       managedNamespace.getDataChangeLastModifiedBy());

    return managedNamespace;
  }

  @Transactional
  public void createPrivateNamespace(String appId, String clusterName, String createBy) {

    //load all private app namespace
    List<AppNamespace> privateAppNamespaces = appNamespaceService.findPrivateAppNamespace(appId);
    //create all private namespace
    for (AppNamespace appNamespace : privateAppNamespaces) {
      Namespace ns = new Namespace();
      ns.setAppId(appId);
      ns.setClusterName(clusterName);
      ns.setNamespaceName(appNamespace.getName());
      ns.setDataChangeCreatedBy(createBy);
      ns.setDataChangeLastModifiedBy(createBy);
      namespaceRepository.save(ns);
      auditService.audit(Namespace.class.getSimpleName(), ns.getId(), Audit.OP.INSERT, createBy);
    }

  }


}
