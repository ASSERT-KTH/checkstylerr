package com.ctrip.apollo.biz.service;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.repository.AppNamespaceRepository;
import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.exception.ServiceException;

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

  public List<AppNamespace> findPublicAppNamespaces(){
    return appNamespaceRepository.findByNameNot(ConfigConsts.NAMESPACE_DEFAULT);
  }
}
