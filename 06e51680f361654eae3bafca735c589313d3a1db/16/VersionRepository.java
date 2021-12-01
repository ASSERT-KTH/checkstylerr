package com.ctrip.apollo.biz.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ctrip.apollo.biz.entity.Version;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface VersionRepository extends PagingAndSortingRepository<Version, Long> {

  Version findByClusterIdAndName(Long id, String name);

  List<Version> findByClusterId(Long clusterId);
}
