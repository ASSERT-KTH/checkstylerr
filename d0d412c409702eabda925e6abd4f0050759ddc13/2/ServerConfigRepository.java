package com.ctrip.apollo.biz.repository;

import com.ctrip.apollo.biz.entity.ServerConfig;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ServerConfigRepository extends PagingAndSortingRepository<ServerConfig, Long> {
  ServerConfig findByKey(String key);
}
