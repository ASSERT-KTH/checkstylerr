package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.po.UserInfo;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface UserService {
  List<UserInfo> searchUsers(String keyword, int offset, int limit);

  UserInfo findByUserId(String userId);

  List<UserInfo> findByUserIds(List<String> userIds);
}
