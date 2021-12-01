package com.ctrip.framework.apollo.portal.controller;



import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceSyncModel;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.service.ConfigService;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
@RequestMapping("")
public class ConfigController {

  @Autowired
  private ConfigService configService;
  @Autowired
  private ServerConfigService serverConfigService;

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public void modifyItemsByText(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody NamespaceTextModel model) {

    checkModel(model != null);

    model.setAppId(appId);
    model.setClusterName(clusterName);
    model.setEnv(env);
    model.setNamespaceName(namespaceName);

    configService.updateConfigItemByText(model);
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.POST)
  public ItemDTO createItem(@PathVariable String appId, @PathVariable String env,
                            @PathVariable String clusterName, @PathVariable String namespaceName,
                            @RequestBody ItemDTO item){
    checkModel(isValidItem(item));

    return configService.createItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.PUT)
  public void updateItem(@PathVariable String appId, @PathVariable String env,
                            @PathVariable String clusterName, @PathVariable String namespaceName,
                            @RequestBody ItemDTO item){
    checkModel(isValidItem(item));

    configService.updateItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }


  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}", method = RequestMethod.DELETE)
  public void deleteItem(@PathVariable String appId, @PathVariable String env,
                         @PathVariable String clusterName, @PathVariable String namespaceName,
                          @PathVariable long itemId){
    if (itemId <= 0){
      throw new BadRequestException("item id invalid");
    }
    configService.deleteItem(Env.valueOf(env), itemId);
  }

  @PreAuthorize(value = "@permissionValidator.hasReleaseNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/release", method = RequestMethod.POST, consumes = {
      "application/json"})
  public ReleaseDTO createRelease(@PathVariable String appId,
                                  @PathVariable String env, @PathVariable String clusterName,
                                  @PathVariable String namespaceName, @RequestBody NamespaceReleaseModel model) {

    checkModel(model != null);
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);

    return configService.createRelease(model);

  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public List<ItemDTO> findItems(@PathVariable String appId, @PathVariable String env,
                                 @PathVariable String clusterName, @PathVariable String namespaceName){

    return configService.findItems(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  @RequestMapping(value = "/namespaces/{namespaceName}/diff", method = RequestMethod.POST, consumes = {
      "application/json"})
  public List<ItemDiffs> diff(@RequestBody NamespaceSyncModel model){
    checkModel(model != null && !model.isInvalid());

    return configService.compare(model.getSyncToNamespaces(), model.getSyncItems());
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public ResponseEntity<Void> update(@PathVariable String appId, @PathVariable String namespaceName,
                                     @RequestBody NamespaceSyncModel model){
    checkModel(model != null && !model.isInvalid());

    configService.syncItems(model.getSyncToNamespaces(), model.getSyncItems());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  private boolean isValidItem(ItemDTO item){
    return item != null && !StringUtils.isContainEmpty(item.getKey());
  }


}
