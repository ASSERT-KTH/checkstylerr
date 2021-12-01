package com.ctrip.framework.apollo.biz.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.ctrip.framework.apollo.biz.entity.Release;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseRepository extends PagingAndSortingRepository<Release, Long> {

  Release findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(@Param("appId") String appId, @Param("clusterName") String clusterName,
                                                                                         @Param("namespaceName") String namespaceName);

  List<Release> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId, String clusterName, String namespaceName, Pageable page);

  List<Release> findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(String appId, String clusterName, String namespaceName, Pageable page);
}
