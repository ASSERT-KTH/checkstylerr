package com.ctrip.framework.apollo.portal.extend.ctrip.filters;

import com.ctrip.framework.apollo.portal.extend.UserInfoHolder;
import com.ctrip.framework.apollo.portal.constant.CatEventType;
import com.dianping.cat.Cat;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RecordAccessUserFilter implements Filter {


  private UserInfoHolder userInfoHolder;


  public RecordAccessUserFilter(UserInfoHolder userInfoHolder) {
    this.userInfoHolder = userInfoHolder;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    Cat.logEvent(CatEventType.USER_ACCESS, userInfoHolder.getUser().getUserId());

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

  }
}
