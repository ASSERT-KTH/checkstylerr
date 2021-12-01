package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.repository.ItemRepository;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  public Item findOne(long itemId) {
    Item item = itemRepository.findOne(itemId);
    return item;
  }

}
