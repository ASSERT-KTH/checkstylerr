package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.api.AdminServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NamespaceService {

  @Autowired
  private AdminServiceAPI.NamespaceAPI namespaceAPI;
  @Autowired
  private PortalSettings portalSettings;

  public List<AppNamespaceDTO> findPublicAppNamespaces(){
    return namespaceAPI.findPublicAppNamespaces(portalSettings.getFirstEnv());
  }

  public NamespaceDTO save(Env env, NamespaceDTO namespace){
    return namespaceAPI.save(env, namespace);
  }

}
