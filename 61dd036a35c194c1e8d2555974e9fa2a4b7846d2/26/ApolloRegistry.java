package com.ctrip.apollo.client.model;

import com.google.common.base.MoreObjects;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloRegistry {
  private String appId;
  private String clusterName;
  private String namespace;

  public ApolloRegistry(String appId, String clusterName, String namespace) {
    this.appId = appId;
    this.clusterName = clusterName;
    this.namespace = namespace;
  }

  public String getAppId() {
    return appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getNamespace() {
    return namespace;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("appId", appId)
        .add("clusterName", clusterName)
        .add("namespace", namespace)
        .toString();
  }
}
