package com.ctrip.framework.apollo.portal.extend;

import com.ctrip.framework.apollo.portal.entity.po.UserInfo;

/**
 * Get access to the user's information,
 * different companies should have a different implementation
 */
public interface UserInfoHolder {

  UserInfo getUser();

}
