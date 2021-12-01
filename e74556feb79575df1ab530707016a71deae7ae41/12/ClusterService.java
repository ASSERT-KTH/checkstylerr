package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.repository.ClusterRepository;

@Service
public class ClusterService {

  @Autowired
  private ClusterRepository clusterRepository;

  public Cluster findOne(String appId, String name) {
    return clusterRepository.findByAppIdAndName(appId, name);
  }
}
