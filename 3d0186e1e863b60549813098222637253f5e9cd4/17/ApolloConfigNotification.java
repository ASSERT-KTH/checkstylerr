package com.ctrip.apollo.core.dto;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigNotification {
  private String appId;
  private String cluster;
  private String namespace;

  //for json converter
  public ApolloConfigNotification() {
  }

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

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
}
