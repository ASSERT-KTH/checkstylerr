package com.ctrip.apollo.biz.service;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.exception.ServiceException;
import com.ctrip.apollo.core.utils.StringUtils;

@Service
public class AppNamespaceService {

  @Autowired
  private AppNamespaceRepository appNamespaceRepository;
  
  @Autowired
  private AuditService auditService;
  
  public boolean isAppNamespaceNameUnique(String appId, String namespaceName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(namespaceName, "Namespace must not be null");
    return Objects.isNull(appNamespaceRepository.findByAppIdAndName(appId, namespaceName));
  }

  public AppNamespace findByNamespaceName(String namespaceName) {
    Preconditions.checkArgument(namespaceName != null, "Namespace must not be null");
    return appNamespaceRepository.findByName(namespaceName);
  }

  public AppNamespace findOne(String appId, String namespaceName){
    Preconditions.checkArgument(!StringUtils.isContainEmpty(appId, namespaceName), "appId or Namespace must not be null");
    return appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
  }

  @Transactional
  public void createDefaultAppNamespace(String appId, String createBy) {
    if (!isAppNamespaceNameUnique(appId, appId)) {
      throw new ServiceException("appnamespace not unique");
    }
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(ConfigConsts.NAMESPACE_DEFAULT);
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
    appNamespace.setDataChangeCreatedBy(createBy);
    appNamespace.setDataChangeLastModifiedBy(createBy);
     appNamespace = appNamespaceRepository.save(appNamespace);

    auditService.audit(AppNamespace.class.getSimpleName(), appNamespace.getId(), Audit.OP.INSERT,
                       createBy);
    return appNamespace;
  }

  public List<AppNamespace> findPublicAppNamespaces(){
    return appNamespaceRepository.findByNameNot(ConfigConsts.NAMESPACE_DEFAULT);
  }

  public AppNamespace update(AppNamespace appNamespace){
    AppNamespace managedNs = appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(), appNamespace.getName());
    BeanUtils.copyEntityProperties(appNamespace, managedNs);
    managedNs = appNamespaceRepository.save(managedNs);

    auditService.audit(AppNamespace.class.getSimpleName(), managedNs.getId(), Audit.OP.UPDATE, managedNs.getDataChangeLastModifiedBy());

    return managedNs;
  }
}
