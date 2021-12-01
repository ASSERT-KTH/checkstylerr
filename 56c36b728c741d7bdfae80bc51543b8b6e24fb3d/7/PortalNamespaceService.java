package com.ctrip.apollo.portal.service;

import com.google.gson.Gson;

import com.ctrip.apollo.common.utils.ExceptionUtils;
import com.ctrip.apollo.core.dto.AppNamespaceDTO;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.NamespaceVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class PortalNamespaceService {

  private Logger logger = LoggerFactory.getLogger(PortalNamespaceService.class);

  @Autowired
  private AdminServiceAPI.ItemAPI itemAPI;

  @Autowired
  private AdminServiceAPI.ReleaseAPI releaseAPI;

  @Autowired
  private AdminServiceAPI.NamespaceAPI namespaceAPI;

  @Autowired
  private PortalSettings portalSettings;

  private Gson gson = new Gson();


  public List<AppNamespaceDTO> findPublicAppNamespaces(){
    return namespaceAPI.findPublicAppNamespaces(portalSettings.getFirstAliveEnv());
  }

  public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace){
    return namespaceAPI.createNamespace(env, namespace);
  }

  public void createAppNamespace(AppNamespaceDTO appNamespace) {
    for (Env env : portalSettings.getActiveEnvs()) {
      try {
        namespaceAPI.createAppNamespace(env, appNamespace);
      } catch (HttpStatusCodeException e) {
        logger.error(ExceptionUtils.toString(e));
        throw e;
      }
    }
  }

  /**
   * load cluster all namespace info with items
   */
  public List<NamespaceVO> findNampspaces(String appId, Env env, String clusterName) {

    List<NamespaceDTO> namespaces = namespaceAPI.findNamespaceByCluster(appId, env, clusterName);
    if (namespaces == null || namespaces.size() == 0) {
      return Collections.emptyList();
    }

    List<NamespaceVO> namespaceVOs = new LinkedList<>();
    for (NamespaceDTO namespace : namespaces) {

      NamespaceVO namespaceVO = null;
      try {
        namespaceVO = parseNamespace(appId, env, clusterName, namespace);
        namespaceVOs.add(namespaceVO);
      } catch (Exception e) {
        logger.error("parse namespace error. app id:{}, env:{}, clusterName:{}, namespace:{}",
                     appId, env, clusterName, namespace.getNamespaceName(), e);
        throw e;
      }
    }

    return namespaceVOs;
  }

  @SuppressWarnings("unchecked")
  private NamespaceVO parseNamespace(String appId, Env env, String clusterName, NamespaceDTO namespace) {
    NamespaceVO namespaceVO = new NamespaceVO();
    namespaceVO.setNamespace(namespace);

    List<NamespaceVO.ItemVO> itemVos = new LinkedList<>();
    namespaceVO.setItems(itemVos);

    String namespaceName = namespace.getNamespaceName();

    //latest Release
    ReleaseDTO release = null;
    Map<String, String> releaseItems = new HashMap<>();
    try {
      release = releaseAPI.loadLatestRelease(appId, env, clusterName, namespaceName);
      releaseItems = gson.fromJson(release.getConfigurations(), Map.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn(ExceptionUtils.toString(e));
      } else {
        throw e;
      }
    }

    //not Release config items
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

    return namespaceVO;
  }

  private NamespaceVO.ItemVO parseItemVO(ItemDTO itemDTO, Map<String, String> releaseItems) {
    String key = itemDTO.getKey();
    NamespaceVO.ItemVO itemVO = new NamespaceVO.ItemVO();
    itemVO.setItem(itemDTO);
    String newValue = itemDTO.getValue();
    String oldValue = releaseItems.get(key);
    if (!StringUtils.isEmpty(key) && (oldValue == null || !newValue.equals(oldValue))) {
      itemVO.setModified(true);
      itemVO.setOldValue(oldValue == null ? "" : oldValue);
      itemVO.setNewValue(newValue);
    }
    return itemVO;
  }

}
