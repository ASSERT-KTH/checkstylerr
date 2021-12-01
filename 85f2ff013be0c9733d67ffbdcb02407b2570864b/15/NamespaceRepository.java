package com.ctrip.apollo.biz.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.biz.entity.Namespace;

public interface NamespaceRepository extends PagingAndSortingRepository<Namespace, Long>{

}
