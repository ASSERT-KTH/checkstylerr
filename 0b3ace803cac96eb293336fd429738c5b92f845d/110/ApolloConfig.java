package com.ctrip.framework.apollo.core.dto;

import com.google.common.base.MoreObjects;

import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfig {

  private String appId;

  private String cluster;

  private String namespaceName;

  private Map<String, String> configurations;

  private String releaseKey;

  public ApolloConfig() {
  }

  public ApolloConfig(String appId,
                      String cluster,
                      String namespaceName,
                      String releaseKey) {
    this.appId = appId;
    this.cluster = cluster;
    this.namespaceName = namespaceName;
    this.releaseKey = releaseKey;
  }

  public String getAppId() {
    return appId;
  }

  public String getCluster() {
    return cluster;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public String getReleaseKey() {
    return releaseKey;
  }

  public Map<String, String> getConfigurations() {
    return configurations;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public void setReleaseKey(String releaseKey) {
    this.releaseKey = releaseKey;
  }

  public void setConfigurations(Map<String, String> configurations) {
    this.configurations = configurations;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("appId", appId)
        .add("cluster", cluster)
        .add("namespaceName", namespaceName)
        .add("releaseKey", releaseKey)
        .add("configurations", configurations)
        .toString();
  }
}
