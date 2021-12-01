package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceCreationModel;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceVO;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceCreationEvent;
import com.ctrip.framework.apollo.portal.service.NamespaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ctrip.framework.apollo.portal.util.RequestPrecondition.checkArgument;
import static com.ctrip.framework.apollo.portal.util.RequestPrecondition.checkModel;

@RestController
public class NamespaceController {

  Logger logger = LoggerFactory.getLogger(NamespaceController.class);

  @Autowired
  private ApplicationEventPublisher publisher;
  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping("/appnamespaces/public")
  public List<AppNamespace> findPublicAppNamespaces() {
    return namespaceService.findPublicAppNamespaces();
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/namespaces", method = RequestMethod.POST)
  public ResponseEntity<Void> createNamespace(@PathVariable String appId, @RequestBody List<NamespaceCreationModel> models) {

    checkModel(!CollectionUtils.isEmpty(models));

    for (NamespaceCreationModel model : models) {
      NamespaceDTO namespace = model.getNamespace();

      checkArgument(model.getEnv(), namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());

      try {
        namespaceService.createNamespace(Env.valueOf(model.getEnv()), namespace);
      } catch (Exception e) {
        logger.error("create namespace error.", e);
      }
    }
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public void createAppNamespace(@PathVariable String appId, @RequestBody AppNamespace appNamespace) {

    checkArgument(appNamespace.getAppId(), appNamespace.getName());

    String operator = userInfoHolder.getUser().getUserId();
    if (StringUtils.isEmpty(appNamespace.getDataChangeCreatedBy())) {
      appNamespace.setDataChangeCreatedBy(operator);
    }
    appNamespace.setDataChangeLastModifiedBy(operator);
    AppNamespace createdAppNamespace = namespaceService.createAppNamespaceInLocal(appNamespace);

    publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));

  }

  @RequestMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceVO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                          @PathVariable String clusterName) {

    return namespaceService.findNampspaces(appId, Env.valueOf(env), clusterName);
  }

}
