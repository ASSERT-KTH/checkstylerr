package com.ctrip.framework.apollo.adminservice.aop;


import com.google.common.collect.Maps;
import com.google.gson.Gson;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * unlock namespace if is redo operation.
 * --------------------------------------------
 * For example: If namespace has a item K1 = v1
 * --------------------------------------------
 * First operate: change k1 = v2 (lock namespace)
 * Second operate: change k1 = v1 (unlock namespace)
 *
 */
@Aspect
@Component
public class NamespaceUnlockAspect {

  private Gson gson = new Gson();

  @Autowired
  private NamespaceLockService namespaceLockService;
  @Autowired
  private NamespaceService namespaceService;
  @Autowired
  private ItemService itemService;
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private BizConfig bizConfig;


  //create item
  @After("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                ItemDTO item) {
    tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
  }

  //update item
  @After("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, itemId, item, ..)")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName, long itemId,
                                ItemDTO item) {
    tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
  }

  //update by change set
  @After("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)")
  public void requireLockAdvice(String appId, String clusterName, String namespaceName,
                                ItemChangeSets changeSet) {
    tryUnlock(namespaceService.findOne(appId, clusterName, namespaceName));
  }

  //delete item
  @After("@annotation(PreAcquireNamespaceLock) && args(itemId, operator, ..)")
  public void requireLockAdvice(long itemId, String operator) {
    Item item = itemService.findOne(itemId);
    if (item == null) {
      throw new BadRequestException("item not exist.");
    }
    tryUnlock(namespaceService.findOne(item.getNamespaceId()));
  }

  private void tryUnlock(Namespace namespace) {
    if (bizConfig.isNamespaceLockSwitchOff()) {
      return;
    }

    if (!isModified(namespace)) {
      namespaceLockService.unlock(namespace.getId());
    }

  }

  boolean isModified(Namespace namespace) {
    Release release = releaseService.findLatestActiveRelease(namespace);
    List<Item> items = itemService.findItems(namespace.getId());

    if (release == null) {
      return hasNormalItems(items);
    }

    Map<String, String> releasedConfiguration = gson.fromJson(release.getConfigurations(), GsonType.CONFIG);
    Map<String, String> configurationFromItems = Maps.newHashMap();

    for (Item item : items) {
      String key = item.getKey();
      if (StringUtils.isBlank(key)) {
        continue;
      }
      //added
      if (releasedConfiguration.get(key) == null) {
        return true;
      }
      configurationFromItems.put(key, item.getValue());
    }

    for (Map.Entry<String, String> entry : releasedConfiguration.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      //deleted or modified
      if (!Objects.equals(configurationFromItems.get(key), value)) {
        return true;
      }

    }

    return false;
  }

  private boolean hasNormalItems(List<Item> items) {
    for (Item item : items) {
      if (!StringUtils.isEmpty(item.getKey())) {
        return true;
      }
    }

    return false;
  }

}
