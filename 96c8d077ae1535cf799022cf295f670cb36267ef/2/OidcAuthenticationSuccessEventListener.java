package com.ctrip.framework.apollo.portal.spi.oidc;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class OidcAuthenticationSuccessEventListener implements
    ApplicationListener<AuthenticationSuccessEvent> {

  private static final Logger log = LoggerFactory
      .getLogger(OidcAuthenticationSuccessEventListener.class);

  private final OidcLocalUserService oidcLocalUserService;

  private final ConcurrentMap<String, String> userIdCache = new ConcurrentHashMap<>();

  public OidcAuthenticationSuccessEventListener(
      OidcLocalUserService oidcLocalUserService) {
    this.oidcLocalUserService = oidcLocalUserService;
  }

  @Override
  public void onApplicationEvent(AuthenticationSuccessEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (principal instanceof OidcUser) {
      this.oidcUserLogin((OidcUser) principal);
      return;
    }
    if (principal instanceof Jwt) {
      this.jwtLogin((Jwt) principal);
      return;
    }
    log.warn("principal is neither oidcUser nor jwt, principal=[{}]", principal);
  }

  private void oidcUserLogin(OidcUser oidcUser) {
    if (this.contains(oidcUser.getSubject())) {
      return;
    }
    UserInfo newUserInfo = new UserInfo();
    newUserInfo.setUserId(oidcUser.getSubject());
    newUserInfo.setName(oidcUser.getPreferredUsername());
    newUserInfo.setEmail(oidcUser.getEmail());
    this.oidcLocalUserService.createLocalUser(newUserInfo);
  }

  private boolean contains(String userId) {
    if (this.userIdCache.containsKey(userId)) {
      return true;
    }
    UserInfo userInfo = this.oidcLocalUserService.findByUserId(userId);
    if (userInfo != null) {
      this.userIdCache.put(userId, userId);
      return true;
    }
    return false;
  }

  private void jwtLogin(Jwt jwt) {
    if (this.contains(jwt.getSubject())) {
      return;
    }
    UserInfo newUserInfo = new UserInfo();
    newUserInfo.setUserId(jwt.getSubject());
    this.oidcLocalUserService.createLocalUser(newUserInfo);
  }
}
