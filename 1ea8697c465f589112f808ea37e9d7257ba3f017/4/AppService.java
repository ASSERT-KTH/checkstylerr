package com.ctrip.apollo.portal.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.apollo.common.utils.ExceptionUtils;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.exception.ServiceException;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.AppInfoVO;
import com.ctrip.apollo.portal.entity.ClusterNavTree;

@Service
public class AppService {

  private Logger logger = LoggerFactory.getLogger(AppService.class);

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private PortalSettings portalSettings;

  @Autowired
  private AdminServiceAPI.AppAPI appAPI;

  public List<AppDTO> findAll(Env env) {
    return appAPI.findApps(env);
  }

  public AppInfoVO load(String appId) {
    //轮询环境直到能找到此app的信息
    AppDTO app = null;
    List<Env> missEnvs = new LinkedList<>();
    for (Env env : portalSettings.getEnvs()) {
      try {

        app = appAPI.loadApp(env, appId);

      } catch (HttpClientErrorException e) {
        //not exist maybe because create app fail.
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
          missEnvs.add(env);
          logger.warn("app:{} in {} not exist", appId, env);
        } else {
          logger.error("load app info({}) from env:{} error.", appId, env);
          throw new ServiceException("can not load app from all envs");
        }
      }
    }
    if (app == null) {
      throw new BadRequestException(String.format("invalid app id %s", appId));
    }

    AppInfoVO appInfo = new AppInfoVO();
    appInfo.setApp(app);
    appInfo.setMissEnvs(missEnvs);

    return appInfo;

  }

  public ClusterNavTree buildClusterNavTree(String appId) {
    ClusterNavTree tree = new ClusterNavTree();

    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      ClusterNavTree.Node clusterNode = new ClusterNavTree.Node(env);
      clusterNode.setClusters(clusterService.findClusters(env, appId));
      tree.addNode(clusterNode);
    }
    return tree;
  }

  public void createAppInAllEnvs(AppDTO app) {
    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      try {
        appAPI.createApp(env, app);
      } catch (HttpStatusCodeException e) {
        logger.error(ExceptionUtils.toString(e));
        throw e;
      }
    }
  }

  public void createApp(Env env, AppDTO app) {
    try {
      appAPI.createApp(env, app);
    } catch (HttpStatusCodeException e) {
      logger.error(ExceptionUtils.toString(e));
      throw e;
    }
  }

}
