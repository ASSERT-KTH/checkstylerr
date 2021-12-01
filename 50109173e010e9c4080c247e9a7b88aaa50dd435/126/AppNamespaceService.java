package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.repository.AppNamespaceRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AppNamespaceService {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private AppNamespaceRepository appNamespaceRepository;
  @Autowired
  private RoleInitializationService roleInitializationService;

  /**
   * 公共的app ns,能被其它项目关联到的app ns
   */
  public List<AppNamespace> findPublicAppNamespaces() {
    return appNamespaceRepository.findByIsPublicTrue();
  }

  public AppNamespace findPublicAppNamespace(String namespaceName) {
    return appNamespaceRepository.findByNameAndIsPublic(namespaceName, true);
  }

  public AppNamespace findByAppIdAndName(String appId, String namespaceName) {
    return appNamespaceRepository.findByAppIdAndName(appId, namespaceName);
  }

  @Transactional
  public void createDefaultAppNamespace(String appId) {
    if (!isAppNamespaceNameUnique(appId, appId)) {
      throw new ServiceException("appnamespace not unique");
    }
    AppNamespace appNs = new AppNamespace();
    appNs.setAppId(appId);
    appNs.setName(ConfigConsts.NAMESPACE_APPLICATION);
    appNs.setComment("default app namespace");
    appNs.setFormat(ConfigFileFormat.Properties.getValue());

    String userId = userInfoHolder.getUser().getUserId();
    appNs.setDataChangeCreatedBy(userId);
    appNs.setDataChangeLastModifiedBy(userId);
    appNamespaceRepository.save(appNs);
  }

  public boolean isAppNamespaceNameUnique(String appId, String namespaceName) {
    Objects.requireNonNull(appId, "AppId must not be null");
    Objects.requireNonNull(namespaceName, "Namespace must not be null");
    return Objects.isNull(appNamespaceRepository.findByAppIdAndName(appId, namespaceName));
  }

  @Transactional
  public AppNamespace createAppNamespaceInLocal(AppNamespace appNamespace) {
    // unique check
    if (appNamespace.isPublic() && findPublicAppNamespace(appNamespace.getName()) != null) {
      throw new BadRequestException(appNamespace.getName() + "已存在");
    }

    if (!appNamespace.isPublic() &&
        appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(), appNamespace.getName()) != null) {
      throw new BadRequestException(appNamespace.getName() + "已存在");
    }

    AppNamespace createdAppNamespace = appNamespaceRepository.save(appNamespace);

    //如果是私有的app namespace 要默认初始化权限,如果是公共的,则在关联此namespace的时候初始化权限
    if (!createdAppNamespace.isPublic()) {
      roleInitializationService.initNamespaceRoles(appNamespace.getAppId(), appNamespace.getName());
    }

    return createdAppNamespace;
  }

}
