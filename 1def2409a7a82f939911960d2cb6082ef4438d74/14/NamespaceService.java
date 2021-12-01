package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.NamespaceRepository;
import com.ctrip.apollo.common.utils.BeanUtils;

@Service
public class NamespaceService {

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Transactional
  public void delete(long id) {
    namespaceRepository.delete(id);
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
    return namespaceRepository.save(entity);
  }

  @Transactional
  public Namespace update(Namespace namespace) {
    Namespace managedNamespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(
        namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());
    BeanUtils.copyEntityProperties(namespace, managedNamespace);
    return namespaceRepository.save(managedNamespace);
  }
}
