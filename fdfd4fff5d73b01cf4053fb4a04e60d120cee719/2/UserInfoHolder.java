package com.ctrip.framework.apollo.portal.auth;

import com.ctrip.framework.apollo.portal.entity.po.UserInfo;

/**
 * 获取登录用户的信息,不同的公司应该有不同的实现
 */
public interface UserInfoHolder {

  UserInfo getUser();

}
