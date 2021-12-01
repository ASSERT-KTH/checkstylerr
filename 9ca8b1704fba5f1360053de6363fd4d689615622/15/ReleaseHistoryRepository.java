package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseHistoryRepository extends PagingAndSortingRepository<ReleaseHistory, Long> {
  Page<ReleaseHistory> findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(String appId, String
      clusterName, String namespaceName, Pageable pageable);

  List<ReleaseHistory> findByReleaseId(long releaseId);
}
