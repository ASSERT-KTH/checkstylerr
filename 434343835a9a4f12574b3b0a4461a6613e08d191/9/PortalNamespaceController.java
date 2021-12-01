package com.ctrip.apollo.portal.controller;

import com.ctrip.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.service.PortalNamespaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PortalNamespaceController {

  @Autowired
  private PortalNamespaceService namespaceService;

  @RequestMapping("/appnamespaces/public")
  public List<AppNamespaceDTO> findPublicAppNamespaces(){
    return namespaceService.findPublicAppNamespaces();
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces", method = RequestMethod.POST)
  public NamespaceDTO createNamespace(@PathVariable String env, @RequestBody NamespaceDTO namespace){
    if (StringUtils.isContainEmpty(env, namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName())){
      throw new BadRequestException("request payload contains empty");
    }
    return namespaceService.createNamespace(Env.valueOf(env), namespace);
  }

  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public void createAppNamespace(@PathVariable String appId, @RequestBody AppNamespaceDTO appNamespace){
    if (StringUtils.isContainEmpty(appId, appNamespace.getAppId(), appNamespace.getName())){
      throw new BadRequestException("request payload contains empty");
    }

    namespaceService.createAppNamespace(appNamespace);
  }

  @RequestMapping("/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceVO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                          @PathVariable String clusterName) {
    if (StringUtils.isContainEmpty(appId, env, clusterName)) {
      throw new BadRequestException("app id and cluster name can not be empty");
    }

    return namespaceService.findNampspaces(appId, Env.valueOf(env), clusterName);
  }

}
