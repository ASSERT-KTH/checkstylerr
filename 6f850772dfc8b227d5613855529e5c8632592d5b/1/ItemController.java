package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.service.ItemService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ItemDTO;

@RestController
public class ItemController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private ItemService itemService;

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public List<ItemDTO> findItems(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    List<Item> items = viewService.findItems(appId, clusterName, namespaceName);
    return BeanUtils.batchTransform(ItemDTO.class, items);
  }

  @RequestMapping("/items/{itemId}")
  public ItemDTO findOne(@PathVariable("itemId") long itemId) {
    Item item = itemService.findOne(itemId);
    return BeanUtils.transfrom(ItemDTO.class, item);
  }
}
