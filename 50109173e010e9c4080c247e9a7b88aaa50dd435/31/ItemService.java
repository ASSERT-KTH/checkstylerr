package com.ctrip.framework.apollo.biz.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ItemService implements InitializingBean {
  private static final int DEFAULT_LIMIT_UPDATE_INTERVAL_IN_SECONDS = 60;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private AuditService auditService;

  @Autowired
  private ServerConfigService serverConfigService;

  private AtomicInteger globalKeyLengthLimit;

  private AtomicInteger globalValueLengthLimit;

  private AtomicReference<Map<Long, Integer>> namespaceValueLengthOverride;

  private Gson gson;

  private ScheduledExecutorService executorService;

  private static final Type namespaceValueLengthOverrideTypeReference =
      new TypeToken<Map<Long, Integer>>() {
      }.getType();

  public ItemService() {
    gson = new Gson();
    globalKeyLengthLimit = new AtomicInteger(128);
    globalValueLengthLimit = new AtomicInteger(20000);
    namespaceValueLengthOverride = new AtomicReference<>();
    executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory
        .create("ItemServiceLimitUpdater", true));
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    executorService.scheduleWithFixedDelay(() -> {
      Transaction transaction = Tracer.newTransaction("Apollo.ItemServiceLimitUpdater", "updateLimit");
      try {
        updateLimits();
        transaction.setStatus(Transaction.SUCCESS);
      } catch (Throwable ex) {
        transaction.setStatus(ex);
      } finally {
        transaction.complete();
      }
    }, 0, getLimitUpdateIntervalInSeconds(), TimeUnit.SECONDS);
  }

  private void updateLimits() {
    String keyLengthLimit = serverConfigService.getValue("item.key.length.limit", null);
    if (!Strings.isNullOrEmpty(keyLengthLimit)) {
      globalKeyLengthLimit.set(Integer.valueOf(keyLengthLimit));
    }
    String valueLengthLimit = serverConfigService.getValue("item.value.length.limit", null);
    if (!Strings.isNullOrEmpty(valueLengthLimit)) {
      globalValueLengthLimit.set(Integer.valueOf(valueLengthLimit));
    }
    String namespaceValueLengthOverrideString =
        serverConfigService.getValue("namespace.value.length.limit.override", null);
    if (!Strings.isNullOrEmpty(namespaceValueLengthOverrideString)) {
      namespaceValueLengthOverride.set(gson.fromJson(
          namespaceValueLengthOverrideString, namespaceValueLengthOverrideTypeReference));
    }
  }

  @Transactional
  public Item delete(long id, String operator) {
    Item item = itemRepository.findOne(id);
    if (item == null) {
      throw new IllegalArgumentException("item not exist. ID:" + id);
    }

    item.setDeleted(true);
    item.setDataChangeLastModifiedBy(operator);
    Item deletedItem = itemRepository.save(item);

    auditService.audit(Item.class.getSimpleName(), id, Audit.OP.DELETE, operator);
    return deletedItem;
  }

  @Transactional
  public int batchDelete(long namespaceId, String operator) {
    return itemRepository.deleteByNamespaceId(namespaceId, operator);

  }

  public Item findOne(String appId, String clusterName, String namespaceName, String key) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    Item item = itemRepository.findByNamespaceIdAndKey(namespace.getId(), key);
    return item;
  }

  public Item findLastOne(String appId, String clusterName, String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(
          String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
    }
    return findLastOne(namespace.getId());
  }

  public Item findLastOne(long namespaceId) {
    return itemRepository.findFirst1ByNamespaceIdOrderByLineNumDesc(namespaceId);
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
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace != null) {
      return findItems(namespace.getId());
    } else {
      return Collections.emptyList();
    }
  }

  @Transactional
  public Item save(Item entity) {
    checkItemKeyLength(entity.getKey());
    checkItemValueLength(entity.getNamespaceId(), entity.getValue());

    entity.setId(0);//protection

    if (entity.getLineNum() == 0) {
      Item lastItem = findLastOne(entity.getNamespaceId());
      int lineNum = lastItem == null ? 1 : lastItem.getLineNum() + 1;
      entity.setLineNum(lineNum);
    }

    Item item = itemRepository.save(entity);

    auditService.audit(Item.class.getSimpleName(), item.getId(), Audit.OP.INSERT,
                       item.getDataChangeCreatedBy());

    return item;
  }

  @Transactional
  public Item update(Item item) {
    checkItemValueLength(item.getNamespaceId(), item.getValue());
    Item managedItem = itemRepository.findOne(item.getId());
    BeanUtils.copyEntityProperties(item, managedItem);
    managedItem = itemRepository.save(managedItem);

    auditService.audit(Item.class.getSimpleName(), managedItem.getId(), Audit.OP.UPDATE,
                       managedItem.getDataChangeLastModifiedBy());

    return managedItem;
  }

  private boolean checkItemValueLength(long namespaceId, String value) {
    int limit = getItemValueLengthLimit(namespaceId);
    if (!StringUtils.isEmpty(value) && value.length() > limit) {
      throw new BadRequestException("value too long. length limit:" + limit);
    }
    return true;
  }

  private boolean checkItemKeyLength(String key) {
    if (!StringUtils.isEmpty(key) && key.length() > globalKeyLengthLimit.get()) {
      throw new BadRequestException("key too long. length limit:" + globalKeyLengthLimit.get());
    }
    return true;
  }

  private int getItemValueLengthLimit(long namespaceId) {
    if (namespaceValueLengthOverride.get() != null && namespaceValueLengthOverride.get()
        .containsKey(namespaceId)) {
      return namespaceValueLengthOverride.get().get(namespaceId);
    }
    return globalValueLengthLimit.get();
  }

  private int getLimitUpdateIntervalInSeconds() {
    return DEFAULT_LIMIT_UPDATE_INTERVAL_IN_SECONDS;
  }
}
