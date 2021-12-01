package com.ctrip.apollo.biz.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.biz.entity.Group;

public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {

  List<Group> findByAppIdAndClusterName(String appId, String clusterName);

  Group findByAppIdAndClusterNameAndGroupName(String appId, String clusterName, String groupName);
}
