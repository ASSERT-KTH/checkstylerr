package com.ctrip.framework.apollo.biz.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.framework.apollo.biz.entity.Namespace;

public interface NamespaceRepository extends PagingAndSortingRepository<Namespace, Long> {

  List<Namespace> findByAppIdAndClusterName(String appId, String clusterName);

  Namespace findByAppIdAndClusterNameAndNamespaceName(String appId, String clusterName, String namespaceName);
}
