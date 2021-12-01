package com.ctrip.framework.apollo.biz.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.exception.NotFoundException;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private AuditService auditService;

  @Transactional
  public void delete(long id, String owner) {
    itemRepository.delete(id);

    auditService.audit(Item.class.getSimpleName(), id, Audit.OP.DELETE, owner);
  }

  public Item findOne(String appId, String clusterName, String namespaceName, String key) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
        clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    Item item = itemRepository.findByNamespaceIdAndKey(namespace.getId(), key);
    return item;
  }

  public Item findLastOne(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
                                                                                        clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    Item item = itemRepository.findFirst1ByNamespaceIdOrderByLineNumDesc(namespace.getId());
    return item;
  }

  public Item findOne(long itemId) {
    Item item = itemRepository.findOne(itemId);
    return item;
  }

  public List<Item> findItems(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespaceId);
    if (items == null) {
      return Collections.emptyList();
    }
    return items;
  }
  
  public List<Item> findItems(String appId, String clusterName, String namespaceName) {
    Namespace group = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
        namespaceName);
    if (group != null) {
      return findItems(group.getId());
    } else {
      return Collections.emptyList();
    }
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
    managedItem = itemRepository.save(managedItem);

    auditService.audit(Item.class.getSimpleName(), managedItem.getId(), Audit.OP.UPDATE,
        managedItem.getDataChangeLastModifiedBy());

    return managedItem;
  }

}
