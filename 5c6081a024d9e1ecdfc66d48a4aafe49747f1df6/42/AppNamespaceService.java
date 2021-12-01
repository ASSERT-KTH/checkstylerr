package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.exception.ServiceException;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.repository.AppNamespaceRepository;

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


  /**
   * 公共的app ns,能被其它项目关联到的app ns
   * @return
   */
  public List<AppNamespace> findPublicAppNamespaces() {
    return appNamespaceRepository.findByNameNotAndIsPublic(ConfigConsts.NAMESPACE_APPLICATION, true);
  }

  public AppNamespace findPublicAppNamespace(String namespaceName){
    return appNamespaceRepository.findByNameAndIsPublic(namespaceName, true);
  }

  public AppNamespace findByAppIdAndName(String appId, String namespaceName){
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
    //not unique
    if (appNamespaceRepository.findByName(appNamespace.getName()) != null){
      throw new BadRequestException(appNamespace.getName() + "已存在");
    }
    AppNamespace managedAppNamespace = appNamespaceRepository.findByAppIdAndName(appNamespace.getAppId(), appNamespace.getName());
    //update
    if (managedAppNamespace != null){
      BeanUtils.copyEntityProperties(appNamespace, managedAppNamespace);
      return appNamespaceRepository.save(managedAppNamespace);
    }else {
      return appNamespaceRepository.save(appNamespace);
    }
  }

}
