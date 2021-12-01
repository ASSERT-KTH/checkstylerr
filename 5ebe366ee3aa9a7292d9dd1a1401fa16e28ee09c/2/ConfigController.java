package com.ctrip.apollo.portal.controller;


import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.form.NamespaceTextModel;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.apollo.portal.service.ConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("")
public class ConfigController {

  @Autowired
  private ConfigService configService;

  @RequestMapping("/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceVO> findNamespaces(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName) {
    if (StringUtils.isContainEmpty(appId, env, clusterName)) {
      throw new BadRequestException("app id and cluster name can not be empty");
    }

    return configService.findNampspaces(appId, Env.valueOf(env), clusterName);
  }

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public void modifyItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody NamespaceTextModel model) {

    if (model == null) {
      throw new BadRequestException("request payload shoud not be null");
    }
    model.setAppId(appId);
    model.setClusterName(clusterName);
    model.setEnv(env);
    model.setNamespaceName(namespaceName);

    if (model.isInvalid()) {
      throw new BadRequestException("request model is invalid");
    }

    configService.updateConfigItemByText(model);
  }

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/release", method = RequestMethod.POST, consumes = {
      "application/json"})
  public ReleaseDTO createRelease(@PathVariable String appId,
      @PathVariable String env, @PathVariable String clusterName,
      @PathVariable String namespaceName, @RequestBody NamespaceReleaseModel model) {
    if (model == null) {
      throw new BadRequestException("request payload shoud not be null");
    }
    model.setAppId(appId);
    model.setClusterName(clusterName);
    model.setEnv(env);
    model.setNamespaceName(namespaceName);

    if (model.isInvalid()) {
      throw new BadRequestException("request model is invalid");
    }

    return configService.createRelease(model);

  }

}
