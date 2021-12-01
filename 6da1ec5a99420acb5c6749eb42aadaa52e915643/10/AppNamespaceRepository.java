package com.ctrip.apollo.biz.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.biz.entity.AppNamespace;

public interface AppNamespaceRepository extends PagingAndSortingRepository<AppNamespace, Long>{

}
