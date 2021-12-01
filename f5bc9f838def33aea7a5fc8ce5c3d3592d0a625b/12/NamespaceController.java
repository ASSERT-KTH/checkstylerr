package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceCreationModel;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceVO;
import com.ctrip.framework.apollo.portal.listener.AppNamespaceCreationEvent;
import com.ctrip.framework.apollo.portal.service.AppNamespaceService;
import com.ctrip.framework.apollo.portal.service.AppService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.service.RoleInitializationService;
import com.dianping.cat.Cat;

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

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkArgument;
import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class NamespaceController {

  Logger logger = LoggerFactory.getLogger(NamespaceController.class);

  @Autowired
  private AppService appService;

  @Autowired
  private ApplicationEventPublisher publisher;
  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private NamespaceService namespaceService;
  @Autowired
  private AppNamespaceService appNamespaceService;
  @Autowired
  private RoleInitializationService roleInitializationService;

  @RequestMapping("/appnamespaces/public")
  public List<AppNamespace> findPublicAppNamespaces() {
    return appNamespaceService.findPublicAppNamespaces();
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateNamespacePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/namespaces", method = RequestMethod.POST)
  public ResponseEntity<Void> createNamespace(@PathVariable String appId,
                                              @RequestBody List<NamespaceCreationModel> models) {

    checkModel(!CollectionUtils.isEmpty(models));
    roleInitializationService.initNamespaceRoles(appId, models.get(0).getNamespace().getNamespaceName());

    for (NamespaceCreationModel model : models) {
      NamespaceDTO namespace = model.getNamespace();

      checkArgument(model.getEnv(), namespace.getAppId(), namespace.getClusterName(), namespace.getNamespaceName());

      try {
        // TODO: 16/6/17 某些环境创建失败,统一处理这种场景
        namespaceService.createNamespace(Env.valueOf(model.getEnv()), namespace);
      } catch (Exception e) {
        logger.error("create namespace fail.", e);
        Cat.logError(
            String.format("create namespace fail. (env=%s namespace=%s)", model.getEnv(), namespace.getNamespaceName()), e);
      }
    }
    return ResponseEntity.ok().build();
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateAppNamespacePermission(#appId, #appNamespace)")
  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public AppNamespace createAppNamespace(@PathVariable String appId, @RequestBody AppNamespace appNamespace) {

    checkArgument(appNamespace.getAppId(), appNamespace.getName());
    if (!InputValidator.isValidAppNamespace(appNamespace.getName())) {
      throw new BadRequestException(String.format("Namespace格式错误: %s",
                                                  InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE + " & "
                                                  + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE));
    }

    //add app org id as prefix
    App app = appService.load(appId);
    StringBuilder appNamespaceName = new StringBuilder();
    //add prefix postfix
    appNamespaceName
        .append(appNamespace.isPublic() ? app.getOrgId() + "." : "")
        .append(appNamespace.getName())
        .append(appNamespace.formatAsEnum() == ConfigFileFormat.Properties ? "" : "." + appNamespace.getFormat());
    appNamespace.setName(appNamespaceName.toString());

    String operator = userInfoHolder.getUser().getUserId();
    if (StringUtils.isEmpty(appNamespace.getDataChangeCreatedBy())) {
      appNamespace.setDataChangeCreatedBy(operator);
    }
    appNamespace.setDataChangeLastModifiedBy(operator);
    AppNamespace createdAppNamespace = appNamespaceService.createAppNamespaceInLocal(appNamespace);

    publisher.publishEvent(new AppNamespaceCreationEvent(createdAppNamespace));

    return createdAppNamespace;
  }

  @RequestMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces")
  public List<NamespaceVO> findNamespaces(@PathVariable String appId, @PathVariable String env,
                                          @PathVariable String clusterName) {

    return namespaceService.findNamespaces(appId, Env.valueOf(env), clusterName);
  }

}
