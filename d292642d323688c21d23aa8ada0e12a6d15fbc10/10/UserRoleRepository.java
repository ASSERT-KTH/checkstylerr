package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.UserRole;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {
  /**
   * find user roles by userId
   * @param userId
   * @return
   */
  List<UserRole> findByUserId(String userId);

  /**
   * find user roles by roleId
   * @param roleId
   * @return
   */
  List<UserRole> findByRoleId(long roleId);

  /**
   * find user roles by userIds and roleId
   * @param userId
   * @param roleId
   * @return
   */
  List<UserRole> findByUserIdInAndRoleId(Collection<String> userId, long roleId);
}
