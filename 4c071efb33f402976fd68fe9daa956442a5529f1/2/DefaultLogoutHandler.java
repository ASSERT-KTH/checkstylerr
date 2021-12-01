package com.ctrip.framework.apollo.portal.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultLogoutHandler implements LogoutHandler{

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    try {
      response.sendRedirect("/");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
