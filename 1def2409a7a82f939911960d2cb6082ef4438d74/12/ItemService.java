package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.common.utils.BeanUtils;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  @Transactional
  public void delete(long id) {
    itemRepository.delete(id);
  }

  public Item findOne(long itemId) {
    Item item = itemRepository.findOne(itemId);
    return item;
  }

  @Transactional
  public Item save(Item item) {
    return itemRepository.save(item);
  }

  @Transactional
  public Item update(Item item) {
    Item managedItem = itemRepository.findOne(item.getId());
    BeanUtils.copyEntityProperties(item, managedItem);
    return itemRepository.save(managedItem);
  }

}
