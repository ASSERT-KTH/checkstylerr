package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseVO;
import com.ctrip.framework.apollo.portal.service.ReleaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class ReleaseController {

  @Autowired
  private ReleaseService releaseService;

  @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/release")
  public ReleaseDTO createRelease(@PathVariable String appId,
                                  @PathVariable String env, @PathVariable String clusterName,
                                  @PathVariable String namespaceName, @RequestBody NamespaceReleaseModel model) {

    checkModel(model != null);
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);

    return releaseService.createRelease(model);
  }


  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
  public List<ReleaseVO> findReleases(@PathVariable String appId,
                                      @PathVariable String env, @PathVariable String clusterName,
                                      @PathVariable String namespaceName,
                                      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size){

    RequestPrecondition.checkNumberPositive(size);
    RequestPrecondition.checkNumberNotNegative(page);

    return releaseService.findReleases(appId, Env.valueOf(env), clusterName, namespaceName, page, size);

  }
}
