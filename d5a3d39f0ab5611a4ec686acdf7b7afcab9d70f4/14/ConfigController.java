package com.ctrip.apollo.configservice.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.dianping.cat.Cat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/configs")
public class ConfigController {
  @Autowired
  private ConfigService configService;

  private Gson gson = new Gson();
  private Type configurationTypeReference =
      new TypeToken<Map<java.lang.String, java.lang.String>>() {
      }.getType();

  @RequestMapping(value = "/{appId}/{clusterName}", method = RequestMethod.GET)
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
                                  @RequestParam(value = "datacenter", required = false) String datacenter,
                                  @RequestParam(value = "releaseId", defaultValue = "-1") String clientSideReleaseId,
                                  HttpServletResponse response) throws IOException {
    return this.queryConfig(appId, clusterName, ConfigConsts.NAMESPACE_DEFAULT, datacenter,
        clientSideReleaseId, response);
  }

  @RequestMapping(value = "/{appId}/{clusterName}/{namespace}", method = RequestMethod.GET)
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
                                  @PathVariable String namespace,
                                  @RequestParam(value = "datacenter", required = false) String datacenter,
                                  @RequestParam(value = "releaseId", defaultValue = "-1") String clientSideReleaseId,
                                  HttpServletResponse response) throws IOException {
    List<Release> releases = Lists.newLinkedList();

    Release currentAppRelease = configService.findRelease(appId, clusterName, namespace);

    if (currentAppRelease != null) {
      releases.add(currentAppRelease);
    }

    //if namespace is not appId itself, should check if it has its own configurations
    if (!Objects.equals(ConfigConsts.NAMESPACE_DEFAULT, namespace)) {
      //TODO find id for this particular namespace, if not equal to current app id, then do more
      if (!Objects.isNull(datacenter)) {
        //TODO load newAppId+datacenter+namespace configurations
      }
      //TODO if load from DC failed, then load newAppId+defaultCluster+namespace configurations
    }

    if (releases.isEmpty()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format(
              "Could not load configurations with appId: %s, clusterName: %s, namespace: %s",
              appId, clusterName, namespace));
      Cat.logEvent("Apollo.Config.NotFound",
          assembleKey(appId, clusterName, namespace, datacenter));
      return null;
    }

    String mergedReleaseId = FluentIterable.from(releases).transform(
        input -> String.valueOf(input.getId())).join(Joiner.on("|"));

    if (mergedReleaseId.equals(clientSideReleaseId)) {
      // Client side configuration is the same with server side, return 304
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      Cat.logEvent("Apollo.Config.NotModified",
          assembleKey(appId, clusterName, namespace, datacenter));
      return null;
    }

    ApolloConfig apolloConfig = new ApolloConfig(appId, clusterName, namespace, mergedReleaseId);
    apolloConfig.setConfigurations(mergeReleaseConfigurations(releases));

    Cat.logEvent("Apollo.Config.Found", assembleKey(appId, clusterName, namespace, datacenter));
    return apolloConfig;
  }

  /**
   * Merge configurations of releases.
   * Release in lower index override those in higher index
   */
  Map<String, String> mergeReleaseConfigurations(List<Release> releases) {
    Map<String, String> result = Maps.newHashMap();
    for (Release release : Lists.reverse(releases)) {
      result.putAll(gson.fromJson(release.getConfigurations(), configurationTypeReference));
    }
    return result;
  }

  private String assembleKey(String appId, String cluster, String namespace, String datacenter) {
    String key = String.format("%s-%s-%s", appId, cluster, namespace);
    if (!Strings.isNullOrEmpty(datacenter)) {
      key += "-" + datacenter;
    }
    return key;
  }

}
