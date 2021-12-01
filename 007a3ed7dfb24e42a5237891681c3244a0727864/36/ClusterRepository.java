package com.ctrip.framework.apollo.biz.repository;


import com.ctrip.framework.apollo.biz.entity.Cluster;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ClusterRepository extends PagingAndSortingRepository<Cluster, Long> {

  List<Cluster> findByAppId(String appId);

  Cluster findByAppIdAndName(String appId, String name);
}
