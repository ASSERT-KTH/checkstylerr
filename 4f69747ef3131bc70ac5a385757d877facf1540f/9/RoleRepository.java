package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.Role;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {
  /**
   * find role by role name
   * @param roleName
   * @return
   */
  Role findTopByRoleName(String roleName);
}
