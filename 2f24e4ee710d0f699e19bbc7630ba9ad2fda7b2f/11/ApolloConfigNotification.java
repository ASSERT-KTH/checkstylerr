package com.ctrip.apollo.core.dto;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigNotification {
  private final String appId;
  private final String cluster;
  private final String namespace;

  public ApolloConfigNotification(String appId, String cluster, String namespace) {
    this.appId = appId;
    this.cluster = cluster;
    this.namespace = namespace;
  }

  public String getAppId() {
    return appId;
  }

  public String getCluster() {
    return cluster;
  }

  public String getNamespace() {
    return namespace;
  }
}
