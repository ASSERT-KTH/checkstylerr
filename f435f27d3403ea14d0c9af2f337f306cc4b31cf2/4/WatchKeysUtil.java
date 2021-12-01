package com.ctrip.framework.apollo.configservice.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.ConfigConsts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class WatchKeysUtil {
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  @Autowired
  private AppNamespaceService appNamespaceService;

  public Set<String> assembleAllWatchKeys(String appId, String clusterName, String namespace,
                                          String dataCenter) {
    Set<String> watchedKeys = assembleWatchKeys(appId, clusterName, namespace, dataCenter);

    //Listen on more namespaces if it's a public namespace
    if (!namespaceBelongsToAppId(appId, namespace)) {
      watchedKeys.addAll(this.findPublicConfigWatchKey(appId, clusterName, namespace, dataCenter));
    }

    return watchedKeys;

  }

  private Set<String> findPublicConfigWatchKey(String applicationId, String clusterName,
                                               String namespace,
                                               String dataCenter) {
    AppNamespace appNamespace = appNamespaceService.findPublicNamespaceByName(namespace);

    //check whether the namespace's appId equals to current one
    if (Objects.isNull(appNamespace) || Objects.equals(applicationId, appNamespace.getAppId())) {
      return Sets.newHashSet();
    }

    String publicConfigAppId = appNamespace.getAppId();

    return assembleWatchKeys(publicConfigAppId, clusterName, namespace, dataCenter);
  }

  private String assembleKey(String appId, String cluster, String namespace) {
    return STRING_JOINER.join(appId, cluster, namespace);
  }

  private Set<String> assembleWatchKeys(String appId, String clusterName, String namespace,
                                        String dataCenter) {
    Set<String> watchedKeys = Sets.newHashSet();

    //watch specified cluster config change
    if (!Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, clusterName)) {
      watchedKeys.add(assembleKey(appId, clusterName, namespace));
    }

    //watch data center config change
    if (!Strings.isNullOrEmpty(dataCenter) && !Objects.equals(dataCenter, clusterName)) {
      watchedKeys.add(assembleKey(appId, dataCenter, namespace));
    }

    //watch default cluster config change
    watchedKeys.add(assembleKey(appId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace));

    return watchedKeys;
  }

  private boolean namespaceBelongsToAppId(String appId, String namespaceName) {
    //Every app has an 'application' namespace
    if (Objects.equals(ConfigConsts.NAMESPACE_APPLICATION, namespaceName)) {
      return true;
    }

    AppNamespace appNamespace = appNamespaceService.findOne(appId, namespaceName);

    return appNamespace != null;
  }
}
