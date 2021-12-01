package com.ctrip.apollo.configservice.controller;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.dto.ApolloConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
  @Autowired
  private ConfigService configService;

  @RequestMapping(value = "/{appId}/{clusterName}", method = RequestMethod.GET)
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
                                  @RequestParam(value = "releaseId", defaultValue = "-1") long clientSideReleaseId,
                                  HttpServletResponse response) throws IOException {
    return this.queryConfig(appId, clusterName, ConfigConsts.NAMESPACE_APPLICATION, clientSideReleaseId,
            response);
  }

  @RequestMapping(value = "/{appId}/{clusterName}/{namespace}", method = RequestMethod.GET)
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
                                  @PathVariable String namespace,
                                  @RequestParam(value = "releaseId", defaultValue = "-1") long clientSideReleaseId,
                                  HttpServletResponse response) throws IOException {
    Release release = configService.findRelease(appId, clusterName, namespace);
    if (release == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format(
              "Could not load version with appId: %s, clusterName: %s, namespace: %s",
              appId, clusterName, namespace));
      return null;
    }
    if (release.getId() == clientSideReleaseId) {
      // Client side configuration is the same with server side, return 304
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return null;
    }

    ApolloConfig apolloConfig = configService.loadConfig(release, namespace);

    if (apolloConfig == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format("Could not load config with releaseId: %d, clusterName: %s",
              release.getId(), clusterName));
      return null;
    }

    return apolloConfig;
  }
}
