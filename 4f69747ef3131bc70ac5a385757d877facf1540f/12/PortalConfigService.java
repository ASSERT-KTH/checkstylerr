package com.ctrip.framework.apollo.portal.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.dto.ItemChangeSets;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;
import com.ctrip.framework.apollo.core.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.exception.NotFoundException;
import com.ctrip.framework.apollo.core.exception.ServiceException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceIdentifer;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.service.txtresolver.ConfigTextResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class PortalConfigService {

  private Logger logger = LoggerFactory.getLogger(PortalConfigService.class);

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private AdminServiceAPI.NamespaceAPI namespaceAPI;
  @Autowired
  private AdminServiceAPI.ItemAPI itemAPI;
  @Autowired
  private AdminServiceAPI.ReleaseAPI releaseAPI;
  @Autowired
  private ConfigTextResolver resolver;


  /**
   * parse config text and update config items
   *
   * @return parse result
   */
  public void updateConfigItemByText(NamespaceTextModel model) {
    String appId = model.getAppId();
    Env env = model.getEnv();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();
    long namespaceId = model.getNamespaceId();
    String configText = model.getConfigText();

    ItemChangeSets changeSets = resolver.resolve(namespaceId, configText,
                                                 itemAPI.findItems(appId, env, clusterName, namespaceName));
    if (changeSets.isEmpty()) {
      return;
    }

    try {
      changeSets.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
      itemAPI.updateItems(appId, env, clusterName, namespaceName, changeSets);
    } catch (Exception e) {
      logger.error("itemAPI.updateItems error. appId{},env:{},clusterName:{},namespaceName:{}", appId, env, clusterName,
                   namespaceName);
      throw new ServiceException(e.getMessage());
    }
  }


  public ItemDTO createOrUpdateItem(String appId, Env env, String clusterName, String namespaceName, ItemDTO item) {
    NamespaceDTO namespace = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    if (namespace == null) {
      throw new BadRequestException(
          "namespace:" + namespaceName + " not exist in env:" + env + ", cluster:" + clusterName);
    }
    String username = userInfoHolder.getUser().getUserId();
    if (StringUtils.isEmpty(item.getDataChangeCreatedBy())) {
      item.setDataChangeCreatedBy(username);
    }
    item.setDataChangeLastModifiedBy(username);
    item.setNamespaceId(namespace.getId());
    return itemAPI.createOrUpdateItem(appId, env, clusterName, namespaceName, item);
  }

  public void deleteItem(Env env, long itemId) {
    itemAPI.deleteItem(env, itemId, userInfoHolder.getUser().getUserId());
  }

  /**
   * createRelease config items
   */
  public ReleaseDTO createRelease(NamespaceReleaseModel model) {
    return releaseAPI.release(model.getAppId(), model.getEnv(), model.getClusterName(),
                              model.getNamespaceName(), model.getReleaseBy(), model.getReleaseComment()
        , userInfoHolder.getUser().getUserId());
  }

  public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespaceName) {
    return itemAPI.findItems(appId, env, clusterName, namespaceName);
  }

  public void syncItems(List<NamespaceIdentifer> comparedNamespaces, List<ItemDTO> sourceItems) {
    List<ItemDiffs> itemDiffs = compare(comparedNamespaces, sourceItems);
    for (ItemDiffs itemDiff : itemDiffs) {
      NamespaceIdentifer namespaceIdentifer = itemDiff.getNamespace();
      ItemChangeSets changeSets = itemDiff.getDiffs();
      changeSets.setDataChangeLastModifiedBy(userInfoHolder.getUser().getUserId());
      try {
        itemAPI
            .updateItems(namespaceIdentifer.getAppId(), namespaceIdentifer.getEnv(),
                         namespaceIdentifer.getClusterName(),
                         namespaceIdentifer.getNamespaceName(), changeSets);
      } catch (HttpClientErrorException e) {
        logger.error("sync items error. namespace:{}", namespaceIdentifer);
        throw new ServiceException(String.format("sync item error. env:%s, clusterName:%s", namespaceIdentifer.getEnv(),
                                                 namespaceIdentifer.getClusterName()), e);
      }
    }
  }

  public List<ItemDiffs> compare(List<NamespaceIdentifer> comparedNamespaces, List<ItemDTO> sourceItems) {

    List<ItemDiffs> result = new LinkedList<>();

    for (NamespaceIdentifer namespace : comparedNamespaces) {

      ItemDiffs itemDiffs = new ItemDiffs(namespace);
      itemDiffs.setDiffs(parseChangeSets(namespace, sourceItems));
      result.add(itemDiffs);
    }

    return result;
  }

  private long getNamespaceId(NamespaceIdentifer namespaceIdentifer) {
    String appId = namespaceIdentifer.getAppId();
    String clusterName = namespaceIdentifer.getClusterName();
    String namespaceName = namespaceIdentifer.getNamespaceName();
    Env env = namespaceIdentifer.getEnv();
    NamespaceDTO namespaceDTO = null;
    try {
      namespaceDTO = namespaceAPI.loadNamespace(appId, env, clusterName, namespaceName);
    } catch (NotFoundException e) {
      logger.warn("namespace not exist. appId:{}, env:{}, clusterName:{}, namespaceName:{}", appId, env, clusterName,
                  namespaceName);
      throw new BadRequestException(String.format(
          "namespace not exist. appId:%s, env:%s, clusterName:%s, namespaceName:%s", appId, env, clusterName,
          namespaceName));
    }
    return namespaceDTO.getId();
  }

  private ItemChangeSets parseChangeSets(NamespaceIdentifer namespace, List<ItemDTO> sourceItems) {
    ItemChangeSets changeSets = new ItemChangeSets();
    List<ItemDTO>
        targetItems =
        itemAPI.findItems(namespace.getAppId(), namespace.getEnv(),
                          namespace.getClusterName(), namespace.getNamespaceName());

    long namespaceId = getNamespaceId(namespace);

    if (CollectionUtils.isEmpty(targetItems)) {//all source items is added
      int lineNum = 1;
      for (ItemDTO sourceItem : sourceItems) {
        changeSets.addCreateItem(buildItem(namespaceId, lineNum++, sourceItem));
      }
    } else {
      Map<String, ItemDTO> targetItemMap = BeanUtils.mapByKey("key", targetItems);
      String key, sourceValue, sourceComment;
      ItemDTO targetItem = null;
      int maxLineNum = targetItems.size();//append to last
      for (ItemDTO sourceItem : sourceItems) {
        key = sourceItem.getKey();
        sourceValue = sourceItem.getValue();
        sourceComment = sourceItem.getComment();
        targetItem = targetItemMap.get(key);

        if (targetItem == null) {//added items

          changeSets.addCreateItem(buildItem(namespaceId, ++maxLineNum, sourceItem));

        } else if (isModified(sourceValue, targetItem.getValue(), sourceComment,
                              targetItem.getComment())) {//modified items
          targetItem.setValue(sourceValue);
          targetItem.setComment(sourceComment);
          changeSets.addUpdateItem(targetItem);
        }
      }

      //parse deleted items
      List<ItemDTO> deletedItems = new LinkedList<>();
      Map<String, ItemDTO> sourceItemMap = BeanUtils.mapByKey("key", sourceItems);
      for (ItemDTO item : targetItems) {
        if (sourceItemMap.get(item.getKey()) == null) {
          deletedItems.add(item);
        }
      }
      changeSets.setDeleteItems(deletedItems);
    }

    return changeSets;
  }

  private ItemDTO buildItem(long namespaceId, int lineNum, ItemDTO sourceItem) {
    ItemDTO createdItem = new ItemDTO();
    BeanUtils.copyEntityProperties(sourceItem, createdItem);
    createdItem.setLineNum(lineNum++);
    createdItem.setNamespaceId(namespaceId);
    return createdItem;
  }

  private boolean isModified(String sourceValue, String targetValue, String sourceComment, String targetComment) {

    if (!sourceValue.equals(targetValue)) {
      return true;
    }

    if (sourceComment == null) {
      return !StringUtils.isEmpty(targetComment);
    } else if (targetComment != null) {
      return !sourceComment.equals(targetComment);
    } else {
      return false;
    }
  }
}
