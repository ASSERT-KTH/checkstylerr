package com.ctrip.apollo.biz.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Group;
import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.GroupRepository;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
import com.google.common.base.Strings;

/**
 * config service for admin
 */
@Service
public class ViewService {

  @Autowired
  private ClusterRepository clusterRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private VersionRepository versionRepository;

  @Autowired
  private ItemRepository itemRepository;
  
  @Autowired
  private ReleaseRepository releaseRepository;

  public List<Cluster> findClusters(String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      return Collections.EMPTY_LIST;
    }

    List<Cluster> clusters = clusterRepository.findByAppId(appId);
    if (clusters == null) {
      return Collections.EMPTY_LIST;
    }
    return clusters;
  }

  public List<Group> findGroups(Long clusterId) {
    List<Group> groups = groupRepository.findByClusterId(clusterId);
    if (groups == null) {
      return Collections.EMPTY_LIST;
    }
    return groups;
  }

  public List<Version> findVersions(Long clusterId) {
    List<Version> versions = versionRepository.findByClusterId(clusterId);
    if (versions == null) {
      return Collections.EMPTY_LIST;
    }
    return versions;
  }
  
  public List<Item> findItems(Long groupId) {
    List<Item> items = itemRepository.findByGroupId(groupId);
    if (items == null) {
      return Collections.EMPTY_LIST;
    }
    return items;
  }
  
  public List<Release> findReleases(Long groupId){
    List<Release> releases = releaseRepository.findByGroupId(groupId);
    if(releases==null){
      return Collections.EMPTY_LIST;
    }
    return releases;
  }
}
