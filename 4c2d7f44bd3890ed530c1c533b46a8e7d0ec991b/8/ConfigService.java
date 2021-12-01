package com.ctrip.apollo.portal.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.service.txtresolver.ConfigTextResolver;
import com.ctrip.apollo.portal.service.txtresolver.TextResolverResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ConfigService {

  private Logger logger = LoggerFactory.getLogger(ConfigService.class);

  @Autowired
  private AdminServiceAPI.NamespaceAPI groupAPI;
  @Autowired
  private AdminServiceAPI.ItemAPI itemAPI;
  @Autowired
  private AdminServiceAPI.ReleaseAPI releaseAPI;

  @Autowired
  private ConfigTextResolver resolver;

  private ObjectMapper objectMapper = new ObjectMapper();

  public List<NamespaceVO> findNampspaces(String appId, Apollo.Env env, String clusterName) {

    List<NamespaceDTO> namespaces = groupAPI.findGroupsByAppAndCluster(appId, env, clusterName);
    if (namespaces == null || namespaces.size() == 0) {
      return Collections.EMPTY_LIST;
    }

    List<NamespaceVO> namespaceVOs = new LinkedList<>();
    for (NamespaceDTO namespace : namespaces) {
      namespaceVOs.add(parseNamespace(appId, env, clusterName, namespace));
    }

    return namespaceVOs;
  }

  public TextResolverResult resolve(String appId, Apollo.Env env, String clusterName, String namespaceName,
                                         String configText) {
    TextResolverResult result = resolver.resolve(configText, itemAPI.findItems(appId, env, clusterName, namespaceName));
    if (result.getCode() == TextResolverResult.Code.OK) {
      ItemChangeSets changeSets = result.getChangeSets();
      //invoke admin service
    }
    return result;
  }

  private NamespaceVO parseNamespace(String appId, Apollo.Env env, String clusterName, NamespaceDTO namespace) {

    NamespaceVO namespaceVO = new NamespaceVO();
    namespaceVO.setNamespace(namespace);

    List<NamespaceVO.ItemVO> itemVos = new LinkedList<>();

    String namespaceName = namespace.getNamespaceName();

    //latest release
    ReleaseDTO release = releaseAPI.loadLatestRelease(appId, env, clusterName, namespaceName);
    Map<String, String> releaseItems = new HashMap<>();
    if (release != null) {
      try {
        releaseItems = objectMapper.readValue(release.getConfigurations(), Map.class);
      } catch (IOException e) {
        logger.error("parse release json error. appId:{},env:{},clusterName:{},namespace:{}", appId,
                     env, clusterName, namespaceName);
      }
    }

    //not release config items
    List<ItemDTO> items = itemAPI.findItems(appId, env, clusterName, namespaceName);
    int modifiedItemCnt = 0;
    for (ItemDTO itemDTO : items) {

      NamespaceVO.ItemVO itemVO = parseItemVO(itemDTO, releaseItems);

      if (itemVO.isModified()) {
        modifiedItemCnt++;
      }

      itemVos.add(itemVO);
    }
    namespaceVO.setItemModifiedCnt(modifiedItemCnt);
    namespaceVO.setItems(itemVos);

    return namespaceVO;
  }

  private NamespaceVO.ItemVO parseItemVO(ItemDTO itemDTO, Map<String, String> releaseItems) {
    NamespaceVO.ItemVO itemVO = new NamespaceVO.ItemVO();
    itemVO.setItem(itemDTO);
    String key = itemDTO.getKey();
    String newValue = itemDTO.getValue();
    String oldValue = releaseItems.get(key);
    if (oldValue == null || !newValue.equals(oldValue)) {
      itemVO.setModified(true);
      itemVO.setOldValue(oldValue == null ? "" : oldValue);
      itemVO.setNewValue(newValue);
    }
    return itemVO;
  }
}
