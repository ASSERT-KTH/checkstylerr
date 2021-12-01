package com.ctrip.framework.apollo.configservice.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.biz.service.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
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
  @Autowired
  private AppNamespaceService appNamespaceService;

  private static final Gson gson = new Gson();
  private static final Type configurationTypeReference =
      new TypeToken<Map<java.lang.String, java.lang.String>>() {
      }.getType();
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);

  @RequestMapping(value = "/{appId}/{clusterName}/{namespace}", method = RequestMethod.GET)
  public ApolloConfig queryConfig(@PathVariable String appId, @PathVariable String clusterName,
                                  @PathVariable String namespace,
                                  @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                  @RequestParam(value = "releaseKey", defaultValue = "-1") String clientSideReleaseKey,
                                  @RequestParam(value = "ip", required = false) String clientIp,
                                  HttpServletResponse response) throws IOException {
    List<Release> releases = Lists.newLinkedList();

    Release currentAppRelease = loadConfig(appId, clusterName, namespace, dataCenter);
    String appClusterNameLoaded = clusterName;

    if (currentAppRelease != null) {
      releases.add(currentAppRelease);
      //we have cluster search process, so the cluster name might be overridden
      appClusterNameLoaded = currentAppRelease.getClusterName();
    }

    //if namespace is not 'application', should check if it's a public configuration
    if (!Objects.equals(ConfigConsts.NAMESPACE_APPLICATION, namespace)) {
      Release publicRelease = this.findPublicConfig(appId, clusterName, namespace, dataCenter);
      if (!Objects.isNull(publicRelease)) {
        releases.add(publicRelease);
      }
    }

    if (releases.isEmpty()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          String.format(
              "Could not load configurations with appId: %s, clusterName: %s, namespace: %s",
              appId, clusterName, namespace));
      Cat.logEvent("Apollo.Config.NotFound",
          assembleKey(appId, clusterName, namespace, dataCenter));
      return null;
    }

    String mergedReleaseKey = FluentIterable.from(releases).transform(
        input -> String.valueOf(input.getReleaseKey())).join(STRING_JOINER);

    if (mergedReleaseKey.equals(clientSideReleaseKey)) {
      // Client side configuration is the same with server side, return 304
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      Cat.logEvent("Apollo.Config.NotModified",
          assembleKey(appId, appClusterNameLoaded, namespace, dataCenter));
      return null;
    }

    ApolloConfig apolloConfig = new ApolloConfig(appId, appClusterNameLoaded, namespace, mergedReleaseKey);
    apolloConfig.setConfigurations(mergeReleaseConfigurations(releases));

    Cat.logEvent("Apollo.Config.Found", assembleKey(appId, appClusterNameLoaded, namespace, dataCenter));
    return apolloConfig;
  }

  /**
   * @param applicationId the application which uses public config
   * @param namespace     the namespace
   * @param dataCenter    the datacenter
   */
  private Release findPublicConfig(String applicationId, String clusterName, String namespace, String dataCenter) {
    AppNamespace appNamespace = appNamespaceService.findByNamespaceName(namespace);

    //check whether the namespace's appId equals to current one
    if (Objects.isNull(appNamespace) || Objects.equals(applicationId, appNamespace.getAppId())) {
      return null;
    }

    String publicConfigAppId = appNamespace.getAppId();

    return loadConfig(publicConfigAppId, clusterName, namespace, dataCenter);
  }

  private Release loadConfig(String appId, String clusterName, String namespace, String dataCenter) {
    //load from specified cluster fist
    if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, clusterName)) {
      Release clusterRelease =
          configService.findRelease(appId, clusterName, namespace);

      if (!Objects.isNull(clusterRelease)) {
        return clusterRelease;
      }
    }

    //try to load via data center
    if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, clusterName)) {
      Release dataCenterRelease =
          configService.findRelease(appId, dataCenter, namespace);
      if (!Objects.isNull(dataCenterRelease)) {
        return dataCenterRelease;
      }
    }

    //fallback to default release
    return configService
        .findRelease(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace);
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
    List<String> keyParts = Lists.newArrayList(appId, cluster, namespace);
    if (!Strings.isNullOrEmpty(datacenter)) {
      keyParts.add(datacenter);
    }
    return STRING_JOINER.join(keyParts);
  }

}
