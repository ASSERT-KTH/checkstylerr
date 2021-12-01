package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.apollo.biz.entity.Audit;
import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.common.utils.BeanUtils;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private AuditService auditService;

  @Transactional
  public void delete(long id, String owner) {
    itemRepository.delete(id);

    auditService.audit(Item.class.getSimpleName(), id, Audit.OP.DELETE, owner);
  }

  public Item findOne(long itemId) {
    Item item = itemRepository.findOne(itemId);
    return item;
  }

  @Transactional
  public Item save(Item entity) {
    Item item = itemRepository.save(entity);

    auditService.audit(Item.class.getSimpleName(), item.getId(), Audit.OP.INSERT,
        item.getDataChangeCreatedBy());

    return item;
  }

  @Transactional
  public Item update(Item item) {
    Item managedItem = itemRepository.findOne(item.getId());
    BeanUtils.copyEntityProperties(item, managedItem);
    managedItem =  itemRepository.save(managedItem);
    
    auditService.audit(Item.class.getSimpleName(), managedItem.getId(), Audit.OP.UPDATE,
      managedItem.getDataChangeLastModifiedBy());
    
    return managedItem;
  }

}
