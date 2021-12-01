package com.ctrip.framework.apollo.portal.auth;

import com.ctrip.framework.apollo.portal.entity.po.UserInfo;

/**
 * 不是ctrip的公司默认提供一个假用户
 */
public class NotCtripUserInfoHolder implements UserInfoHolder{


  public NotCtripUserInfoHolder(){

  }

  @Override
  public UserInfo getUser() {
    UserInfo userInfo = new UserInfo();
    userInfo.setUsername("apollo");
    return userInfo;
  }
}
