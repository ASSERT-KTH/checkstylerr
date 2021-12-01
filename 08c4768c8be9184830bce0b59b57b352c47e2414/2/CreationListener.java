package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.ExceptionUtils;
import com.ctrip.framework.apollo.core.dto.AppDTO;
import com.ctrip.framework.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.PortalSettings;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

@Component
public class CreationListener {

  private static Logger logger = LoggerFactory.getLogger(CreationListener.class);

  @Autowired
  private PortalSettings portalSettings;
  @Autowired
  private AdminServiceAPI.AppAPI appAPI;
  @Autowired
  private AdminServiceAPI.NamespaceAPI namespaceAPI;

  @EventListener
  public void onAppCreationEvent(AppCreationEvent event) {
    AppDTO appDTO = BeanUtils.transfrom(AppDTO.class, event.getApp());
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        appAPI.createApp(env, appDTO);
      } catch (HttpStatusCodeException e) {
        logger.error("call appAPI.createApp error.[{app}, {env}]", appDTO.getAppId(), env, e);
      }
    }
  }

  @EventListener
  public void onAppNamespaceCreationEvent(AppNamespaceCreationEvent event){
    AppNamespaceDTO dto = BeanUtils.transfrom(AppNamespaceDTO.class, event.getAppNamespace());
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        namespaceAPI.createOrUpdateAppNamespace(env, dto);
      } catch (HttpStatusCodeException e) {
        logger.error("call namespaceAPI.createOrUpdateAppNamespace error. [{app}, {env}]", dto.getAppId(), env, e);
      }
    }
  }

}
