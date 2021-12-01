package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ItemDTO;

@RestController
public class ItemController {

  @Autowired
  private ViewService viewService;
  
  @RequestMapping("/apps/{appId}/clusters/{clusterId}/groups/{groupId}/items")
  public List<ItemDTO> findItems(
      @PathVariable("groupId") Long groupId) {
    List<Item> items = viewService.findItems(groupId);
    return BeanUtils.batchTransform(ItemDTO.class, items);
  }
}
