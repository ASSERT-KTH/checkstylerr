package com.ctrip.framework.apollo.portal.service;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.ExceptionUtils;
import com.ctrip.framework.apollo.core.dto.AppDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.repository.AppRepository;

@Service
public class AppService {

  private Logger logger = LoggerFactory.getLogger(AppService.class);

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private ClusterService clusterService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private RoleInitializationService roleInitializationService;

  @Autowired
  private AdminServiceAPI.AppAPI appAPI;

  @Autowired
  private AppRepository appRepository;


  public List<App> findAll() {
    Iterable<App> apps = appRepository.findAll();
    if (apps == null) {
      return Collections.EMPTY_LIST;
    }
    return Lists.newArrayList((apps));
  }

  public App load(String appId) {
    App app = appRepository.findByAppId(appId);
    if (app == null){
      throw new BadRequestException(String.format("app %s cant found.", appId));
    }
    return app;
  }

  public AppDTO load(Env env, String appId) {
    return appAPI.loadApp(env, appId);
  }

  public void createApp(Env env, App app) {
    enrichUserInfo(app);
    try {
      AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, app);
      appAPI.createApp(env, appDTO);
    } catch (HttpStatusCodeException e) {
      logger.error(ExceptionUtils.toString(e));
      throw e;
    }
  }

  public void enrichUserInfo(App app) {
    String username = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(username);
    app.setDataChangeLastModifiedBy(username);
  }


  @Transactional
  public App create(App app) {
    String appId = app.getAppId();
    App managedApp = appRepository.findByAppId(appId);

    if (managedApp != null) {
      throw new BadRequestException(String.format("app id %s already exists!", app.getAppId()));
    } else {
      App createdApp = appRepository.save(app);
      appNamespaceService.createDefaultAppNamespace(appId);
      //role
      roleInitializationService.initAppRoles(createdApp);
      return createdApp;
    }
  }

  public EnvClusterInfo createEnvNavNode(Env env, String appId) {
    EnvClusterInfo node = new EnvClusterInfo(env);
    node.setClusters(clusterService.findClusters(env, appId));
    return node;
  }

}
