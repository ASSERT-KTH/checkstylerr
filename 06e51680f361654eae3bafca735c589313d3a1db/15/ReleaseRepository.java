package com.ctrip.apollo.biz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.ctrip.apollo.biz.entity.Release;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseRepository extends PagingAndSortingRepository<Release, Long> {

  @Query("SELECT r FROM VersionReleaseMapping m INNER JOIN Release r on m.releaseId = r.Id INNER JOIN Version v on m.versionId = v.id WHERE r.id = :versionId AND r.groupName = :groupName")
  Release findByGroupNameAndVersionId(@Param("groupName") String groupName, @Param("versionId") long versionId);

  List<Release> findByGroupId(Long groupId);
}
