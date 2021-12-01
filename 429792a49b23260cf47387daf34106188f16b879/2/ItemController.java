package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.service.ItemService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.common.auth.ActiveUser;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.exception.NotFoundException;

@RestController
public class ItemController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private ItemService itemService;

  @RequestMapping(path = "/items/", method = RequestMethod.POST)
  public ResponseEntity<ItemDTO> create(@RequestBody ItemDTO dto, @ActiveUser UserDetails user) {
    Item entity = BeanUtils.transfrom(Item.class, dto);
    entity.setDataChangeCreatedBy(user.getUsername());
    entity = itemService.save(entity);
    dto = BeanUtils.transfrom(ItemDTO.class, entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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
    List<Item> items = viewService.findItems(appId, clusterName, namespaceName);
    return BeanUtils.batchTransform(ItemDTO.class, items);
  }

  @RequestMapping("/items/{itemId}")
  public ItemDTO get(@PathVariable("itemId") long itemId) {
    Item item = itemService.findOne(itemId);
    if (item == null) throw new NotFoundException("item not found for itemId " + itemId);
    return BeanUtils.transfrom(ItemDTO.class, item);
  }

  @RequestMapping(path = "/item/{itemId}", method = RequestMethod.PUT)
  public ItemDTO update(@PathVariable("itemId") long itemId, @RequestBody ItemDTO dto,
      @ActiveUser UserDetails user) {
    Item entity = itemService.findOne(itemId);
    if (entity == null) throw new NotFoundException("item not found for itemId " + itemId);
    entity.setDataChangeLastModifiedBy(user.getUsername());
    entity = itemService.update(BeanUtils.transfrom(Item.class, dto));
    return BeanUtils.transfrom(ItemDTO.class, entity);
  }
}
