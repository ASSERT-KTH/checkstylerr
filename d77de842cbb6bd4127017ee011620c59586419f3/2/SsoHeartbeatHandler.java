package com.ctrip.framework.apollo.portal.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface SsoHeartbeatHandler {
  void doHeartbeat(HttpServletRequest request, HttpServletResponse response);
}
