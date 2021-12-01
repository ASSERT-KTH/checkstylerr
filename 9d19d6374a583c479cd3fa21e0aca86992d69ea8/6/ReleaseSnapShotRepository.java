package com.ctrip.apollo.biz.repository;

import com.ctrip.apollo.biz.entity.ReleaseSnapshot;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseSnapShotRepository
    extends PagingAndSortingRepository<ReleaseSnapshot, Long> {
  ReleaseSnapshot findByReleaseIdAndClusterName(long releaseId, String clusterName);

  List<ReleaseSnapshot> findByReleaseId(long releaseId);
}
