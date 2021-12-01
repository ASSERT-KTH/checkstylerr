package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.NamespaceRepository;
import com.ctrip.apollo.common.utils.BeanUtils;

@Service
public class NamespaceService {

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private AuditService auditService;

  @Transactional
  public void delete(long id, String owner) {
    namespaceRepository.delete(id);

    auditService.audit(Namespace.class.getSimpleName(), id, Audit.OP.DELETE, owner);
  }

  public Namespace findOne(Long namespaceId) {
    return namespaceRepository.findOne(namespaceId);
  }

  public Namespace findOne(String appId, String clusterName, String namespaceName) {
    return namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
        namespaceName);
  }

  @Transactional
  public Namespace save(Namespace entity) {
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
}
