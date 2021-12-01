package com.ctrip.framework.apollo.adminservice.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.common.auth.ActiveUser;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.exception.NotFoundException;

@RestController
public class ItemController {

  @Autowired
  private ItemService itemService;

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.POST)
  public ItemDTO createOrUpdate(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @RequestBody ItemDTO dto,
      @ActiveUser UserDetails user) {
    Item entity = BeanUtils.transfrom(Item.class, dto);
    Item managedEntity = itemService.findOne(appId, clusterName, namespaceName, entity.getKey());
    if (managedEntity != null) {
      managedEntity.setDataChangeLastModifiedBy(user.getUsername());
      BeanUtils.copyEntityProperties(entity, managedEntity);
      entity = itemService.update(managedEntity);
    } else {
      entity.setDataChangeCreatedBy(user.getUsername());
      entity = itemService.save(entity);
    }

    dto = BeanUtils.transfrom(ItemDTO.class, entity);
    return dto;
  }

  @RequestMapping(path = "/items/{itemId}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("itemId") long itemId, @ActiveUser UserDetails user) {
    Item entity = itemService.findOne(itemId);
    if (entity == null) throw new NotFoundException("item not found for itemId " + itemId);
    itemService.delete(entity.getId(), user.getUsername());
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public List<ItemDTO> findItems(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    List<Item> items = itemService.findItems(appId, clusterName, namespaceName);
    List<ItemDTO> itemDTOs = new LinkedList<>();

    for (Item item: items){
      ItemDTO itemDTO = BeanUtils.transfrom(ItemDTO.class, item);
      itemDTO.setLastModifiedBy(item.getDataChangeLastModifiedBy());
      itemDTO.setLastModifiedTime(item.getDataChangeLastModifiedTime());
      itemDTOs.add(itemDTO);
    }
    return itemDTOs;
  }

  @RequestMapping("/items/{itemId}")
  public ItemDTO get(@PathVariable("itemId") long itemId) {
    Item item = itemService.findOne(itemId);
    if (item == null) throw new NotFoundException("item not found for itemId " + itemId);
    return BeanUtils.transfrom(ItemDTO.class, item);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{key}")
  public ItemDTO get(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @PathVariable("key") String key) {
    Item item = itemService.findOne(appId, clusterName, namespaceName, key);
    if (item == null) throw new NotFoundException(
        String.format("item not found for %s %s %s %s", appId, clusterName, namespaceName, key));
    return BeanUtils.transfrom(ItemDTO.class, item);
  }
}
