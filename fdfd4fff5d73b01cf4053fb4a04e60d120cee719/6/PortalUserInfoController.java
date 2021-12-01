package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.auth.CtripUserInfoHolder;
import com.ctrip.framework.apollo.portal.auth.NotCtripUserInfoHolder;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.po.UserInfo;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class PortalUserInfoController {
  private static Logger logger = LoggerFactory.getLogger(PortalUserInfoController.class);

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ServerConfigRepository serverConfigRepository;

  private UserInfoHolder userInfoHolder;

  @PostConstruct
  public void post() {
    try {
      userInfoHolder = applicationContext.getBean(CtripUserInfoHolder.class);
    } catch (NoSuchBeanDefinitionException e) {
      logger.debug("default user info holder");
      userInfoHolder = applicationContext.getBean(NotCtripUserInfoHolder.class);
    }
  }

  @RequestMapping("/user")
  public UserInfo getCurrentUserName() {
    try {
      return userInfoHolder.getUser();
    } catch (Exception e) {
      throw new BadRequestException("请先登录");
    }
  }

  @RequestMapping("/user/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //将session销毁
    request.getSession().invalidate();

    Cookie cookie = new Cookie("memCacheAssertionID", null);
    //将cookie的有效期设置为0，命令浏览器删除该cookie
    cookie.setMaxAge(0);
    cookie.setPath(request.getContextPath() + "/");
    response.addCookie(cookie);

    //重定向到SSO的logout地址
    String casServerUrl = serverConfigRepository.findByKey("casServerUrlPrefix").getValue();
    String serverName = serverConfigRepository.findByKey("casServerUrlPrefix").getValue();

    response.sendRedirect(casServerUrl + "/logout?service=" + serverName);
  }
}
