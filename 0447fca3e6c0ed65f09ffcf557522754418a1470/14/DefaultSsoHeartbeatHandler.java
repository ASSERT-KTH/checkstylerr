package com.ctrip.framework.apollo.portal.auth.defaultimpl;

import com.ctrip.framework.apollo.portal.auth.SsoHeartbeatHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultSsoHeartbeatHandler implements SsoHeartbeatHandler {
  @Override
  public void doHeartbeat(HttpServletRequest request, HttpServletResponse response) {
    try {
      response.getWriter().write("default sso heartbeat handler");
    } catch (IOException e) {
    }
  }
}
