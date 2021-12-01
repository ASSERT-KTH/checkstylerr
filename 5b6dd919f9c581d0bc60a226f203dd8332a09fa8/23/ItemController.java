package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.service.ItemService;
import com.ctrip.framework.apollo.portal.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController("openapiItemController")
@RequestMapping("/openapi/v1/envs/{env}")
public class ItemController {

  @Autowired
  private ItemService itemService;
  @Autowired
  private UserService userService;


  @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.POST)
  public OpenItemDTO createItem(@PathVariable String appId, @PathVariable String env,
                                @PathVariable String clusterName, @PathVariable String
                                    namespaceName,
                                @RequestBody OpenItemDTO item, HttpServletRequest request) {

    RequestPrecondition.checkArguments(
        !StringUtils.isContainEmpty(item.getKey(), item.getValue(), item.getDataChangeCreatedBy()),
        "key,value,dataChangeCreatedBy 字段不能为空");

    if (userService.findByUserId(item.getDataChangeCreatedBy()) == null) {
      throw new BadRequestException("用户不存在.");
    }

    ItemDTO toCreate = OpenApiBeanUtils.transformToItemDTO(item);

    //protect
    toCreate.setLineNum(0);
    toCreate.setId(0);
    toCreate.setDataChangeLastModifiedBy(toCreate.getDataChangeCreatedBy());

    ItemDTO createdItem = itemService.createItem(appId, Env.fromString(env),
        clusterName, namespaceName, toCreate);
    return OpenApiBeanUtils.transformFromItemDTO(createdItem);
  }

  @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}", method = RequestMethod.PUT)
  public void updateItem(@PathVariable String appId, @PathVariable String env,
                         @PathVariable String clusterName, @PathVariable String namespaceName,
                         @PathVariable long itemId, @RequestBody OpenItemDTO item,
                         HttpServletRequest request) {

    RequestPrecondition.checkArguments(item != null && item.getId() > 0 && itemId == item.getId(),
        "item data error");
    RequestPrecondition.checkArguments(
        !StringUtils.isContainEmpty(item.getKey(), item.getValue(), item
            .getDataChangeLastModifiedBy()),
        "key,value,dataChangeLastModifiedBy 字段不能为空");

    if (userService.findByUserId(item.getDataChangeLastModifiedBy()) == null) {
      throw new BadRequestException("用户不存在.");
    }

    ItemDTO toUpdateItem = itemService.loadItem(Env.fromString(env), itemId);
    if (toUpdateItem == null) {
      throw new BadRequestException("item not exist");
    }
    //protect. only value,comment,lastModifiedBy can be modified
    toUpdateItem.setComment(item.getComment());
    toUpdateItem.setValue(item.getValue());
    toUpdateItem.setDataChangeLastModifiedBy(item.getDataChangeLastModifiedBy());

    itemService.updateItem(appId, Env.fromString(env), clusterName, namespaceName,
        toUpdateItem);
  }


  @PreAuthorize(value = "@consumerPermissionValidator.hasModifyNamespacePermission(#request, #appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}", method = RequestMethod.DELETE)
  public void deleteItem(@PathVariable String appId, @PathVariable String env,
                         @PathVariable String clusterName, @PathVariable String namespaceName,
                         @PathVariable long itemId, @RequestParam String operator,
                         HttpServletRequest request) {

    if (userService.findByUserId(operator) == null) {
      throw new BadRequestException("用户不存在.");
    }
    itemService.deleteItem(Env.fromString(env), itemId, operator);
  }

}
