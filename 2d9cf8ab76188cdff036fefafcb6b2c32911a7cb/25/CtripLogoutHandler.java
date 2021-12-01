package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.spi.LogoutHandler;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CtripLogoutHandler implements LogoutHandler {

  @Autowired
  private ServerConfigService serverConfigService;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    //将session销毁
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    Cookie cookie = new Cookie("memCacheAssertionID", null);
    //将cookie的有效期设置为0，命令浏览器删除该cookie
    cookie.setMaxAge(0);
    cookie.setPath(request.getContextPath() + "/");
    response.addCookie(cookie);

    //重定向到SSO的logout地址
    String casServerUrl = serverConfigService.getValue("casServerUrlPrefix");
    String serverName = serverConfigService.getValue("serverName");

    try {
      response.sendRedirect(casServerUrl + "/logout?service=" + serverName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
