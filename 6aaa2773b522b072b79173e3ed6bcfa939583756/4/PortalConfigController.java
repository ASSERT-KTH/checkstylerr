package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceSyncModel;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.service.PortalConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("")
public class PortalConfigController {

  @Autowired
  private PortalConfigService configService;

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public void modifyItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody NamespaceTextModel model) {

    if (model == null) {
      throw new BadRequestException("request payload should not be null");
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

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.POST)
  public ItemDTO createItem(@PathVariable String appId, @PathVariable String env,
                            @PathVariable String clusterName, @PathVariable String namespaceName,
                            @RequestBody ItemDTO item){
    if (StringUtils.isContainEmpty(appId, env, clusterName, namespaceName)){
      throw new BadRequestException("request payload should not be contain empty.");
    }
    if (!isValidItem(item) && item.getNamespaceId() <= 0){
      throw new BadRequestException("request model is invalid");
    }
    return configService.createOrUpdateItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.PUT)
  public ItemDTO updateItem(@PathVariable String appId, @PathVariable String env,
                            @PathVariable String clusterName, @PathVariable String namespaceName,
                            @RequestBody ItemDTO item){
    if (StringUtils.isContainEmpty(appId, env, clusterName, namespaceName)){
      throw new BadRequestException("request payload should not be contain empty.");
    }
    if (!isValidItem(item)){
      throw new BadRequestException("request model is invalid");
    }
    return configService.createOrUpdateItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/release", method = RequestMethod.POST, consumes = {
      "application/json"})
  public ReleaseDTO createRelease(@PathVariable String appId,
                                  @PathVariable String env, @PathVariable String clusterName,
                                  @PathVariable String namespaceName, @RequestBody NamespaceReleaseModel model) {
    if (model == null) {
      throw new BadRequestException("request payload should not be null");
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

  @RequestMapping(value = "/apps/{appId}/env/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public List<ItemDTO> findItems(@PathVariable String appId, @PathVariable String env,
                                 @PathVariable String clusterName, @PathVariable String namespaceName){

    if (StringUtils.isContainEmpty(appId, env, clusterName, namespaceName)){
      throw new BadRequestException("appid,env,cluster name,namespace name can not be empty");
    }

    return configService.findItems(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  @RequestMapping(value = "/namespaces/{namespaceName}/diff", method = RequestMethod.POST, consumes = {
      "application/json"})
  public List<ItemDiffs> diff(@RequestBody NamespaceSyncModel model){
    if (model == null){
      throw new BadRequestException("request payload should not be null");
    }
    if (model.isInvalid()) {
      throw new BadRequestException("request model is invalid");
    }

    return configService.compare(model.getSyncToNamespaces(), model.getSyncItems());
  }

  @RequestMapping(value = "/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public ResponseEntity<Void> update(@RequestBody NamespaceSyncModel model){
    if (model == null){
      throw new BadRequestException("request payload should not be null");
    }
    if (model.isInvalid()) {
      throw new BadRequestException("request model is invalid");
    }
    configService.syncItems(model.getSyncToNamespaces(), model.getSyncItems());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  private boolean isValidItem(ItemDTO item){
    return item != null && !StringUtils.isContainEmpty(item.getKey(), item.getValue());
  }

}
