package com.ctrip.apollo.biz.service;

import com.google.common.base.Strings;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.ConfigItem;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.ConfigItemRepository;
import com.ctrip.apollo.biz.utils.ApolloBeanUtils;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ConfigItemDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * config service for admin
 */
@Service("adminConfigService")
public class AdminConfigService {

  @Autowired
  private ClusterRepository clusterRepository;
  @Autowired
  private ConfigItemRepository configItemRepository;


  public List<ClusterDTO> findClustersByApp(String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      return Collections.EMPTY_LIST;
    }
    List<Cluster> clusters = clusterRepository.findByAppId(appId);
    if (clusters == null || clusters.size() == 0) {
      return Collections.EMPTY_LIST;
    }

    return ApolloBeanUtils.batchTransform(ClusterDTO.class, clusters);
  }

  public List<ConfigItemDTO> findConfigItemsByClusters(List<Long> clusterIds) {
    if (clusterIds == null || clusterIds.size() == 0) {
      return Collections.EMPTY_LIST;
    }
    List<ConfigItem> configItems = configItemRepository.findByClusterIdIsIn(clusterIds);
    if (configItems == null || configItems.size() == 0) {
      return Collections.EMPTY_LIST;
    }

    return ApolloBeanUtils.batchTransform(ConfigItemDTO.class, configItems);
  }

}
