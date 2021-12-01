package com.ctrip.framework.apollo.adminservice.aop;


import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ServerConfigService;
import com.ctrip.framework.apollo.core.dto.ItemChangeSets;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.exception.BadRequestException;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;


/**
 * 一个namespace在一次发布中只能允许一个人修改配置
 * 通过数据库lock表来实现
 */
@Aspect
@Component
public class NamespaceLockAspect {
  private static final Logger logger = LoggerFactory.getLogger(NamespaceLockAspect.class);

  private static final String NAMESPACE_LOCK_SWITCH_CONFIG_KEY = "namespace.lock.switch";

  @Autowired
  private ServerConfigService serverConfigService;
  @Autowired
  private NamespaceLockService namespaceLockService;
  @Autowired
  private NamespaceService namespaceService;
  @Autowired
  private ItemService itemService;


  @Before("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, item, ..)")
  public void createOrUpdateItemRequireLock(String appId, String clusterName, String namespaceName, ItemDTO item) {
    acquireLock(appId, clusterName, namespaceName, item.getDataChangeLastModifiedBy());
  }

  @Before("@annotation(PreAcquireNamespaceLock) && args(appId, clusterName, namespaceName, changeSet, ..)")
  public void createOrUpdateItemRequireLock(String appId, String clusterName, String namespaceName,
                                            ItemChangeSets changeSet) {
    acquireLock(appId, clusterName, namespaceName, changeSet.getDataChangeLastModifiedBy());
  }

  @Before("@annotation(PreAcquireNamespaceLock) && args(itemId, operator, ..)")
  public void deleteItemRequireLock(long itemId, String operator) {
    Item item = itemService.findOne(itemId);

    acquireLock(item.getNamespaceId(), operator);
  }

  private void acquireLock(String appId, String clusterName, String namespaceName, String currentUser) {
    if (isNamespaceLockSwitchOff()) {
      return;
    }

    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);

    acquireLock(namespace, currentUser);
  }

  private void acquireLock(long namespaceId, String currentUser) {
    Namespace namespace = namespaceService.findOne(namespaceId);

    acquireLock(namespace, currentUser);

  }

  private void acquireLock(Namespace namespace, String currentUser) {
    if (namespace == null) {
      throw new BadRequestException("namespace not exist.");
    }

    long namespaceId = namespace.getId();

    NamespaceLock namespaceLock = namespaceLockService.findLock(namespaceId);
    if (namespaceLock == null) {
      try {
        tryLock(namespaceId, currentUser);
        //lock success
      } catch (DataIntegrityViolationException e) {
        //lock fail
        acquireLockFail(namespace, currentUser);
      } catch (Exception e){
        logger.error("try lock error", e);
        throw e;
      }
    } else {
      //check lock owner is current user
      String lockOwner = namespaceLock.getDataChangeCreatedBy();
      if (!lockOwner.equals(currentUser)) {
        acquireLockFail(namespace, currentUser);
      }
    }
  }

  private void tryLock(long namespaceId, String user) {
    NamespaceLock lock = new NamespaceLock();
    lock.setNamespaceId(namespaceId);
    lock.setDataChangeCreatedBy(user);
    lock.setDataChangeLastModifiedBy(user);
    namespaceLockService.tryLock(lock);
  }

  private void acquireLockFail(Namespace namespace, String currentUser){
    NamespaceLock namespaceLock = namespaceLockService.findLock(namespace.getId());
    if (namespaceLock == null){
      acquireLock(namespace, currentUser);
    }
    String lockOwner = namespaceLock.getDataChangeCreatedBy();
    throw new BadRequestException("namespace:" + namespace.getNamespaceName() + " is modifying by " + lockOwner);
  }

  private boolean isNamespaceLockSwitchOff() {
    return !"true".equals(serverConfigService.getValue(NAMESPACE_LOCK_SWITCH_CONFIG_KEY, "false"));
  }

}
