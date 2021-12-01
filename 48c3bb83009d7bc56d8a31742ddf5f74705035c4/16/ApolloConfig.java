package com.ctrip.apollo.core.dto;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfig implements Comparable<ApolloConfig> {

  private String appId;

  private String cluster;

  private String version;

  private Map<String, Object> configurations;

  private long releaseId;

  private int order;

  @JsonCreator
  public ApolloConfig(@JsonProperty("appId") String appId,
                      @JsonProperty("cluster") String cluster,
                      @JsonProperty("version") String version,
                      @JsonProperty("releaseId") long releaseId) {
    super();
    this.appId = appId;
    this.cluster = cluster;
    this.version = version;
    this.releaseId = releaseId;
  }

  public Map<String, Object> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Map<String, Object> configurations) {
    this.configurations = configurations;
  }

  public String getAppId() {
    return appId;
  }

  public String getCluster() {
    return cluster;
  }

  public String getVersion() {
    return version;
  }

  public long getReleaseId() {
    return releaseId;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("appId", appId)
        .add("cluster", cluster)
        .add("version", version)
        .add("releaseId", releaseId)
        .add("configurations", configurations)
        .toString();
  }

  @Override
  public int compareTo(ApolloConfig toCompare) {
    if (toCompare == null || this.getOrder() > toCompare.getOrder()) {
      return 1;
    }
    if (toCompare.getOrder() > this.getOrder()) {
      return -1;
    }
    return 0;
  }
}
