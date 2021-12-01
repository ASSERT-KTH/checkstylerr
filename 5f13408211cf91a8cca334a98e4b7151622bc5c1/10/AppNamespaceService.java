package com.ctrip.framework.apollo.biz.service;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.exception.ServiceException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

@Service
public class AppNamespaceService {

  @Autowired
  private AppNamespaceRepository appNamespaceRepository;
  @Autowired
  private NamespaceService namespaceService;
  @Autowired
  private ClusterService clusterService;
  @Autowired
  private AuditService auditService;
  
  public boolean isAppNamespaceNameUnique(String appId, String namespaceName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(namespaceName, "Namespace must not be null");
    return Objects.isNull(appNamespaceRepository.findByAppIdAndName(appId, namespaceName));
  }

  public AppNamespace findPublicNamespaceByName(String namespaceName) {
    Preconditions.checkArgument(namespaceName != null, "Namespace must not be null");
    return appNamespaceRepository.findByNameAndIsPublicTrue(namespaceName);
  }

  public List<AppNamespace> findPrivateAppNamespace(String appId){
    return appNamespaceRepository.findByAppIdAndIsPublic(appId, false);
  }

  public AppNamespace findOne(String appId, String namespaceName){
    Preconditions.checkArgument(!StringUtils.isContainEmpty(appId, namespaceName), "appId or Namespace must not be null");
    return appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
  }

  @Transactional
  public void createDefaultAppNamespace(String appId, String createBy) {
    if (!isAppNamespaceNameUnique(appId, ConfigConsts.NAMESPACE_APPLICATION)) {
      throw new ServiceException("appnamespace not unique");
    }
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(ConfigConsts.NAMESPACE_APPLICATION);
    appNs.setComment("default app namespace");
    appNs.setDataChangeCreatedBy(createBy);
    appNs.setDataChangeLastModifiedBy(createBy);
    appNamespaceRepository.save(appNs);

    auditService.audit(AppNamespace.class.getSimpleName(), appNs.getId(), Audit.OP.INSERT,
                       createBy);
  }

  @Transactional
  public AppNamespace createAppNamespace(AppNamespace appNamespace, String createBy){
    if (!isAppNamespaceNameUnique(appNamespace.getAppId(), appNamespace.getName())) {
      throw new ServiceException("appnamespace not unique");
    }
    appNamespace.setId(0);//protection
    appNamespace.setDataChangeCreatedBy(createBy);
    appNamespace.setDataChangeLastModifiedBy(createBy);
    appNamespace = appNamespaceRepository.save(appNamespace);
    //所有的cluster下面link新建的appnamespace
    if (!appNamespace.isPublic()){
      String appId = appNamespace.getAppId();
      String namespaceName = appNamespace.getName();

      List<Cluster> clusters = clusterService.findClusters(appId);
      for (Cluster cluster: clusters){
        Namespace namespace = new Namespace();
        namespace.setClusterName(cluster.getName());
        namespace.setAppId(appId);
        namespace.setNamespaceName(namespaceName);
        namespace.setDataChangeCreatedBy(createBy);
        namespace.setDataChangeLastModifiedBy(createBy);
        namespaceService.save(namespace);
      }

    }
    auditService.audit(AppNamespace.class.getSimpleName(), appNamespace.getId(), Audit.OP.INSERT,
                       createBy);
    return appNamespace;
  }

  public AppNamespace update(AppNamespace appNamespace){
    AppNamespace managedNs = appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(), appNamespace.getName());
    BeanUtils.copyEntityProperties(appNamespace, managedNs);
    managedNs = appNamespaceRepository.save(managedNs);

    auditService.audit(AppNamespace.class.getSimpleName(), managedNs.getId(), Audit.OP.UPDATE, managedNs.getDataChangeLastModifiedBy());

    return managedNs;
  }
}
