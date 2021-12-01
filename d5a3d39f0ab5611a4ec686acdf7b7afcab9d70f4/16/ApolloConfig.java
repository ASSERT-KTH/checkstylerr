package com.ctrip.apollo.core.dto;

import com.google.common.base.MoreObjects;

import com.ctrip.apollo.Apollo;

import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfig {

  private String appId;

  private String cluster;

  private String namespace;

  private Map<String, String> configurations;

  private String releaseId;

  public ApolloConfig() {
  }

  public ApolloConfig(String appId,
                      String cluster,
                      String namespace,
                      String releaseId) {
    this.appId = appId;
    this.cluster = cluster;
    this.namespace = namespace;
    this.releaseId = releaseId;
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

  public String getReleaseId() {
    return releaseId;
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

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setReleaseId(String releaseId) {
    this.releaseId = releaseId;
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
        .add("namespace", namespace)
        .add("releaseId", releaseId)
        .add("configurations", configurations)
        .toString();
  }
}
