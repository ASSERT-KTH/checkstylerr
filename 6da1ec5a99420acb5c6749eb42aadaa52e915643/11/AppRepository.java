package com.ctrip.apollo.biz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.ctrip.apollo.biz.entity.App;

public interface AppRepository extends PagingAndSortingRepository<App, Long> {

  @Query("SELECT a from App a WHERE a.name LIKE %:name%")
  List<App> findByName(@Param("name") String name);

  App findByAppId(String appId);

}
