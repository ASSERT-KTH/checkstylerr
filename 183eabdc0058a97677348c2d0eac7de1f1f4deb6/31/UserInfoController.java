package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.auth.LogoutHandler;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.po.UserInfo;
import com.ctrip.framework.apollo.portal.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserInfoController {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private LogoutHandler logoutHandler;

  @Autowired
  private UserService userService;

  @RequestMapping("/user")
  public UserInfo getCurrentUserName() {
      return userInfoHolder.getUser();
  }

  @RequestMapping("/user/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    logoutHandler.logout(request, response);
  }

  @RequestMapping("/users")
  public List<UserInfo> searchUsersByKeyword(@RequestParam(value = "keyword") String keyword,
                                             @RequestParam(value = "offset", defaultValue = "0") int offset,
                                             @RequestParam(value = "limit", defaultValue = "10") int limit) {
    return userService.searchUsers(keyword, offset, limit);
  }

  @RequestMapping("/users/{userId}")
  public UserInfo getUserByUserId(@PathVariable String userId) {
    return userService.findByUserId(userId);
  }
}
