package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceVO;
import com.ctrip.framework.apollo.portal.service.NamespaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ctrip.framework.apollo.portal.util.RequestPrecondition.checkArgument;

@RestController
public class NamespaceController {

  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping("/appnamespaces/public")
  public List<AppNamespace> findPublicAppNamespaces() {
    return namespaceService.findPublicAppNamespaces();
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces", method = RequestMethod.POST)
  public NamespaceDTO createNamespace(@PathVariable String env, @RequestBody NamespaceDTO namespace) {

    checkArgument(namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());

    return namespaceService.createNamespace(Env.valueOf(env), namespace);
  }

  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public void createAppNamespace(@PathVariable String appId, @RequestBody AppNamespaceDTO appNamespace) {

    checkArgument(appNamespace.getAppId(), appNamespace.getName());

    namespaceService.createRemoteAppNamespace(appNamespace);
  }

  @RequestMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceVO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                          @PathVariable String clusterName) {

    return namespaceService.findNampspaces(appId, Env.valueOf(env), clusterName);
  }

}
