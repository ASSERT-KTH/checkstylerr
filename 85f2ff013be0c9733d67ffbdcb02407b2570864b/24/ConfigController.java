package com.ctrip.apollo.configservice.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.core.dto.ApolloConfig;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
  @Autowired
  private ConfigService configService;

  @RequestMapping(value = "/{appId}/{clusterName}/{groupName}/{versionName:.*}", method = RequestMethod.GET)
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String groupName, @PathVariable String versionName,
      @RequestParam(value = "releaseId", defaultValue = "-1") long clientSideReleaseId,
      HttpServletResponse response) throws IOException {
    Release release = configService.findRelease(appId, clusterName, groupName);
    if (release == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format(
              "Could not load version with appId: %s, clusterName: %s, groupName: %s, versionName: %s",
              appId, clusterName, groupName, versionName));
      return null;
    }
    if (release.getId() == clientSideReleaseId) {
      // Client side configuration is the same with server side, return 304
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return null;
    }

    ApolloConfig apolloConfig = configService.loadConfig(release, groupName, versionName);

    if (apolloConfig == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format("Could not load config with releaseId: %d, clusterName: %s",
            release.getId(), clusterName));
      return null;
    }

    return apolloConfig;
  }
}
