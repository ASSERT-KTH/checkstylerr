package com.ctrip.apollo.biz.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.biz.entity.Item;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

  Item findByNamespaceIdAndKey(Long namespaceId, String key);

  List<Item> findByNamespaceIdOrderByLineNumAsc(Long namespaceId);

}
