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
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.entity.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.NamespaceIdentifer;
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
      itemAPI.updateItems(appId, env, clusterName, namespaceName, changeSets);
    } catch (Exception e) {
      logger.error("itemAPI.updateItems error. appId{},env:{},clusterName:{},namespaceName:{}", appId, env, clusterName,
                   namespaceName);
      throw new ServiceException(e.getMessage());
    }
  }


  public ItemDTO createOrUpdateItem(String appId, Env env, String clusterName, String namespaceName, ItemDTO item){
    return itemAPI.createOrUpdateItem(appId, env, clusterName, namespaceName, item);

  }
  /**
   * createRelease config items
   */
  public ReleaseDTO createRelease(NamespaceReleaseModel model) {
    return releaseAPI.release(model.getAppId(), model.getEnv(), model.getClusterName(),
                              model.getNamespaceName(), model.getReleaseBy(), model.getReleaseComment());
  }

  public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespaceName) {
    return itemAPI.findItems(appId, env, clusterName, namespaceName);
  }

  public void syncItems(List<NamespaceIdentifer> comparedNamespaces, List<ItemDTO> sourceItems){
    List<ItemDiffs> itemDiffs = compare(comparedNamespaces, sourceItems);
    for (ItemDiffs itemDiff: itemDiffs){
      NamespaceIdentifer namespaceIdentifer = itemDiff.getNamespace();
      try {
        itemAPI
            .updateItems(namespaceIdentifer.getAppId(), namespaceIdentifer.getEnv(),
                         namespaceIdentifer.getClusterName(),
                         namespaceIdentifer.getNamespaceName(), itemDiff.getDiffs());
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

  private ItemChangeSets parseChangeSets(NamespaceIdentifer namespace, List<ItemDTO> sourceItems){
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
      Map<String, ItemDTO> keyMapItem = BeanUtils.mapByKey("key", targetItems);
      String key, sourceValue, sourceComment;
      ItemDTO targetItem = null;
      int maxLineNum = targetItems.size();//append to last
      for (ItemDTO sourceItem : sourceItems) {
        key = sourceItem.getKey();
        sourceValue = sourceItem.getValue();
        sourceComment = sourceItem.getComment();
        targetItem = keyMapItem.get(key);

        if (targetItem == null) {//added items

          changeSets.addCreateItem(buildItem(namespaceId, ++maxLineNum, sourceItem));

        } else if (!sourceValue.equals(targetItem.getValue()) || !sourceComment
            .equals(targetItem.getComment())) {//modified items
          targetItem.setValue(sourceValue);
          targetItem.setComment(sourceComment);
          changeSets.addUpdateItem(targetItem);
        }
      }
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
}
