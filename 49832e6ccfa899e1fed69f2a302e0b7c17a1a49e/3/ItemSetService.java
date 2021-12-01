package com.ctrip.framework.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.dto.ItemChangeSets;
import com.ctrip.framework.apollo.core.dto.ItemDTO;

@Service
public class ItemSetService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private AuditService auditService;

  @Transactional
  public void updateSet(ItemChangeSets changeSet) {
    String operator = changeSet.getDataChangeLastModifiedBy();
    if (!CollectionUtils.isEmpty(changeSet.getCreateItems())) {
      for (ItemDTO item : changeSet.getCreateItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        entity.setDataChangeCreatedBy(operator);
        entity.setDataChangeLastModifiedBy(operator);
        itemRepository.save(entity);
      }
      auditService.audit("ItemSet", null, Audit.OP.INSERT, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getUpdateItems())) {
      for (ItemDTO item : changeSet.getUpdateItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        Item managedItem = itemRepository.findOne(entity.getId());
        BeanUtils.copyEntityProperties(entity, managedItem);
        managedItem.setDataChangeLastModifiedBy(operator);
        itemRepository.save(managedItem);
      }
      auditService.audit("ItemSet", null, Audit.OP.UPDATE, operator);
    }

    if (!CollectionUtils.isEmpty(changeSet.getDeleteItems())) {
      for (ItemDTO item : changeSet.getDeleteItems()) {
        Item entity = BeanUtils.transfrom(Item.class, item);
        entity.setDeleted(true);
        entity.setDataChangeLastModifiedBy(operator);
        itemRepository.save(entity);
      }
      auditService.audit("ItemSet", null, Audit.OP.DELETE, operator);
    }
  }
}
