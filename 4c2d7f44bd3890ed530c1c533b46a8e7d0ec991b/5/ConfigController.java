package com.ctrip.apollo.portal.controller;


import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.entity.SimpleRestfulResponse;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.entity.SimpleResponse;
import com.ctrip.apollo.portal.service.ConfigService;
import com.ctrip.apollo.portal.service.txtresolver.TextResolverResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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
      throw new IllegalArgumentException("app id and cluster name can not be empty");
    }

    return configService.findNampspaces(appId, Apollo.Env.valueOf(env), clusterName);
  }

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/modify", method = RequestMethod.GET)
  public ResponseEntity<SimpleRestfulResponse> modifyConfigs(@PathVariable String appId, @PathVariable String env,
                                                             @PathVariable String clusterName,
                                                             @PathVariable String namespaceName,
                                                             String configText) {
    TextResolverResult result =
        configService.resolve(appId, Apollo.Env.valueOf(env), clusterName, namespaceName, configText);
    TextResolverResult.Code code = result.getCode();

    if (code == TextResolverResult.Code.OK) {
      return ResponseEntity.status(HttpStatus.OK).body(new SimpleRestfulResponse(code.getValue(), "success"));
    } else {
      return ResponseEntity.status(HttpStatus.OK)
          .body(new SimpleRestfulResponse(code.getValue(), code.getBaseMsg() + result.getExtensionMsg()));
    }
  }

}
