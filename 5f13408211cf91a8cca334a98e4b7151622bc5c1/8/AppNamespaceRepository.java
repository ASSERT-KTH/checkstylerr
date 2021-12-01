package com.ctrip.framework.apollo.biz.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.framework.apollo.common.entity.AppNamespace;

import java.util.List;

public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long>{

  AppNamespace findByAppIdAndName(String appId, String namespaceName);

  AppNamespace findByNameAndIsPublicTrue(String namespaceName);

  List<AppNamespace> findByAppIdAndIsPublic(String appId, boolean isPublic);

}
