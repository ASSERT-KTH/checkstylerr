package com.ctrip.apollo.portal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.dto.AppConfigVO;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.constants.PortalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@Service
public class ConfigService {

  private Logger logger = LoggerFactory.getLogger(ConfigService.class);

  @Autowired
  private AdminServiceAPI.ConfigAPI configAPI;
  @Autowired
  private AdminServiceAPI.ClusterAPI clusterAPI;

  private ObjectMapper objectMapper = new ObjectMapper();

//  public AppConfigVO loadReleaseConfig(Env env, String appId, String cluster, String namespace) {
//
//    if (Strings.isNullOrEmpty(appId) || Strings.isNullOrEmpty(cluster) || Strings.isNullOrEmpty(namespace)) {
//      return null;
//    }
//
//    ReleaseDTO[] releaseSnapShots = configAPI.getConfigByReleaseId(env, releaseId);
//    if (releaseSnapShots == null || releaseSnapShots.length == 0) {
//      return null;
//    }
//
//    AppConfigVO appConfigVO = AppConfigVO.newInstance(appId, versionId);
//
//    for (ReleaseDTO snapShot : releaseSnapShots) {
//      // default cluster
//      if (ConfigConsts.DEFAULT_CLUSTER_NAME.equals(snapShot.getClusterName())) {
//
//        collectDefaultClusterConfigs(appId, snapShot, appConfigVO);
//
//      } else {// cluster special configs
//        collectSpecialClusterConfigs(appId, snapShot, appConfigVO);
//      }
//    }
//    return appConfigVO;
//  }

  private void collectDefaultClusterConfigs(String appId, ReleaseDTO snapShot,
                                            AppConfigVO appConfigVO) {

    Map<String, List<ItemDTO>> groupedConfigs =
        groupConfigsByApp(appId, snapShot.getConfigurations());

    List<AppConfigVO.OverrideAppConfig> overrideAppConfigs = appConfigVO.getOverrideAppConfigs();

    for (Map.Entry<String, List<ItemDTO>> entry : groupedConfigs.entrySet()) {
      String configAppId = entry.getKey();
      List<ItemDTO> kvs = entry.getValue();

      if (configAppId.equals(appId)) {
        appConfigVO.setDefaultClusterConfigs(kvs);
      } else {

        AppConfigVO.OverrideAppConfig overrideAppConfig = new AppConfigVO.OverrideAppConfig();
        overrideAppConfig.setAppId(configAppId);
        overrideAppConfig.setConfigs(kvs);
        overrideAppConfigs.add(overrideAppConfig);
      }
    }

  }

  /**
   * appId -> List<KV>
   */
  private Map<String, List<ItemDTO>> groupConfigsByApp(String selfAppId, String configJson) {
    if (configJson == null || "".equals(configJson)) {
      return Maps.newHashMap();
    }

    Map<String, List<ItemDTO>> appIdMapKVs = new HashMap<>();

    String key;
    Object value;
    Map<String, String> kvMaps = null;
    try {
      kvMaps = objectMapper.readValue(configJson, Map.class);
    } catch (IOException e) {
      logger.error("parse release snapshot json error. app id:{}", selfAppId);
      return Maps.newHashMap();
    }

    for (Map.Entry<String, String> entry : kvMaps.entrySet()) {
      key = entry.getKey();
      value = entry.getValue();

      String appId = getAppIdFromKey(key);
      List<ItemDTO> kvs = appIdMapKVs.get(appId);
      if (kvs == null) {
        kvs = new LinkedList<>();
        appIdMapKVs.put(appId, kvs);
      }
      kvs.add(new ItemDTO(key, value.toString()));
    }

    return appIdMapKVs;

  }

  private String getAppIdFromKey(String key) {
    return key.substring(0, key.indexOf("."));
  }

  private void collectSpecialClusterConfigs(String appId, ReleaseDTO snapShot,
                                            AppConfigVO appConfigVO) {
    List<AppConfigVO.OverrideClusterConfig> overrideClusterConfigs =
        appConfigVO.getOverrideClusterConfigs();
    AppConfigVO.OverrideClusterConfig overrideClusterConfig =
        new AppConfigVO.OverrideClusterConfig();
    overrideClusterConfig.setClusterName(snapShot.getClusterName());
    // todo step1: cluster special config can't override other app config
    overrideClusterConfig.setConfigs(groupConfigsByApp(appId, snapShot.getConfigurations()).get(appId));
    overrideClusterConfigs.add(overrideClusterConfig);
  }

  public AppConfigVO loadLatestConfig(Env env, String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      return null;
    }

    ClusterDTO[] clusters = clusterAPI.getClustersByApp(env, appId);
    if (clusters == null || clusters.length == 0) {
      return null;
    }

    List<Long> clusterIds = new ArrayList<>(clusters.length);
    for (ClusterDTO cluster : clusters) {
      clusterIds.add(cluster.getId());
    }

    ItemDTO[] configItems = configAPI.getLatestConfigItemsByClusters(env, clusterIds);

    return buildAPPConfigVO(appId, Arrays.asList(configItems));
  }

  private AppConfigVO buildAPPConfigVO(String appId, List<ItemDTO> configItems) {
    if (configItems == null || configItems.size() == 0) {
      return null;
    }

    Map<String, List<ItemDTO>> groupedClusterConfigs = groupConfigByCluster(configItems);

    AppConfigVO appConfigVO = AppConfigVO.newInstance(appId, PortalConstants.LASTEST_VERSION_ID);

    groupConfigByAppAndEnrichDTO(groupedClusterConfigs, appConfigVO);

    return appConfigVO;

  }

  private Map<String, List<ItemDTO>> groupConfigByCluster(List<ItemDTO> configItems) {
    Map<String, List<ItemDTO>> groupedClusterConfigs = new HashMap<>();

//    String clusterName;
//    for (ItemDTO configItem : configItems) {
//      clusterName = configItem.getClusterName();
//      List<ItemDTO> clusterConfigs = groupedClusterConfigs.get(clusterName);
//      if (clusterConfigs == null) {
//        clusterConfigs = new LinkedList<>();
//        groupedClusterConfigs.put(clusterName, clusterConfigs);
//      }
//      clusterConfigs.add(configItem);
//    }
    return groupedClusterConfigs;
  }

  private void groupConfigByAppAndEnrichDTO(Map<String, List<ItemDTO>> groupedClusterConfigs,
                                            AppConfigVO appConfigVO) {
    String appId = appConfigVO.getAppId();

    List<ItemDTO> defaultClusterConfigs = appConfigVO.getDefaultClusterConfigs();

    List<AppConfigVO.OverrideAppConfig> overrideAppConfigs = appConfigVO.getOverrideAppConfigs();

    List<AppConfigVO.OverrideClusterConfig> overrideClusterConfigs =
        appConfigVO.getOverrideClusterConfigs();

    String clusterName;
    List<ItemDTO> clusterConfigs;
    for (Map.Entry<String, List<ItemDTO>> entry : groupedClusterConfigs.entrySet()) {
      clusterName = entry.getKey();
      clusterConfigs = entry.getValue();

      if (ConfigConsts.CLUSTER_NAME_DEFAULT.equals(clusterName)) {
        // default cluster configs
        collectDefaultClusterConfigs(appId, clusterConfigs, defaultClusterConfigs,
                                     overrideAppConfigs);
      } else {
        // override cluster configs
        collectSpecialClusterConfigs(clusterName, clusterConfigs, overrideClusterConfigs);
      }
    }
  }

  private void collectDefaultClusterConfigs(String appId, List<ItemDTO> clusterConfigs,
                                            List<ItemDTO> defaultClusterConfigs,
                                            List<AppConfigVO.OverrideAppConfig> overrideAppConfigs) {

    Map<String, AppConfigVO.OverrideAppConfig> appIdMapOverrideAppConfig = null;

//    for (ItemDTO config : clusterConfigs) {
//      String targetAppId = config.getAppId();
//      if (appId.equals(targetAppId)) {// app self's configs
//        defaultClusterConfigs.add(config);
//      } else {// override other app configs
//        if (appIdMapOverrideAppConfig == null) {
//          appIdMapOverrideAppConfig = new HashMap<>();
//        }
//
//        AppConfigVO.OverrideAppConfig overrideAppConfig =
//            appIdMapOverrideAppConfig.get(targetAppId);
//
//        if (overrideAppConfig == null) {
//          overrideAppConfig = new AppConfigVO.OverrideAppConfig();
//          appIdMapOverrideAppConfig.put(targetAppId, overrideAppConfig);
//          overrideAppConfigs.add(overrideAppConfig);
//        }
//
//        overrideAppConfig.setAppId(targetAppId);
//        overrideAppConfig.addConfig(config);
//      }
//    }
  }

  private void collectSpecialClusterConfigs(String clusterName, List<ItemDTO> clusterConfigs,
                                            List<AppConfigVO.OverrideClusterConfig> overrideClusterConfigs) {
    AppConfigVO.OverrideClusterConfig overrideClusterConfig =
        new AppConfigVO.OverrideClusterConfig();
    overrideClusterConfig.setClusterName(clusterName);
    overrideClusterConfig.setConfigs(clusterConfigs);
    overrideClusterConfigs.add(overrideClusterConfig);
  }
}
