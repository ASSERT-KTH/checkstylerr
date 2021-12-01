package com.ctrip.framework.apollo.biz.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.exception.ServiceException;

@Service
public class NamespaceService {

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private AuditService auditService;

  public boolean isNamespaceUnique(String appId, String cluster, String namespace) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(cluster, "Cluster must not be null");
    Objects.requireNonNull(namespace, "Namespace must not be null");
    return Objects.isNull(
        namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, cluster, namespace));
  }

  @Transactional
  public void delete(long id, String operator) {
    Namespace namespace = namespaceRepository.findOne(id);
    if (namespace == null) {
      return;
    }

    namespace.setDeleted(true);
    namespace.setDataChangeLastModifiedBy(operator);
    namespaceRepository.save(namespace);

    auditService.audit(Namespace.class.getSimpleName(), id, Audit.OP.DELETE, operator);
  }

  public Namespace findOne(Long namespaceId) {
    return namespaceRepository.findOne(namespaceId);
  }

  public Namespace findOne(String appId, String clusterName, String namespaceName) {
    return namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
        namespaceName);
  }

  public List<Namespace> findNamespaces(String appId, String clusterName) {
    List<Namespace> groups = namespaceRepository.findByAppIdAndClusterNameOrderByIdAsc(appId, clusterName);
    if (groups == null) {
      return Collections.emptyList();
    }
    return groups;
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
  public void createDefaultNamespace(String appId, String createBy) {
    if (!isNamespaceUnique(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, appId)) {
      throw new ServiceException("namespace not unique");
    }

    Namespace ns = new Namespace();
    ns.setAppId(appId);
    ns.setClusterName(ConfigConsts.CLUSTER_NAME_DEFAULT);
    ns.setNamespaceName(ConfigConsts.NAMESPACE_APPLICATION);
    ns.setDataChangeCreatedBy(createBy);
    ns.setDataChangeLastModifiedBy(createBy);
    namespaceRepository.save(ns);

    auditService.audit(Namespace.class.getSimpleName(), ns.getId(), Audit.OP.INSERT, createBy);
  }
}
