package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;

@Service
public class ItemSetService {

  @Autowired
  private ItemRepository itemRepository;

  public void updateSet(ItemChangeSets changeSet) {
    for (ItemDTO item : changeSet.getCreateItems()) {
      Item entity = BeanUtils.transfrom(Item.class, item);
      itemRepository.save(entity);
    }

    for (ItemDTO item : changeSet.getUpdateItems()) {
      Item entity = BeanUtils.transfrom(Item.class, item);
      Item managedItem = itemRepository.findOne(entity.getId());
      BeanUtils.copyEntityProperties(entity, managedItem);
      itemRepository.save(managedItem);
    }

    for (ItemDTO item : changeSet.getDeletedItems()) {
      Item entity = BeanUtils.transfrom(Item.class, item);
      itemRepository.delete(entity.getId());
    }
  }
}
