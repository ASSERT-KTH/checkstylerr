package com.ctrip.apollo.biz.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.biz.entity.Privilege;

import java.util.List;

public interface PrivilegeRepository extends PagingAndSortingRepository<Privilege, Long> {

  List<Privilege> findByNamespaceId(long namespaceId);

  List<Privilege> findByNamespaceIdAndPrivilType(long namespaceId, String privilType);

  Privilege findByNamespaceIdAndNameAndPrivilType(long namespaceId, String name, String privilType);
}
