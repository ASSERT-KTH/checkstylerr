package com.ctrip.apollo.core.dto;

public class ItemDTO {

  private long id;

  private long clusterId;

  private String clusterName;

  private String appId;

  private String key;

  private String value;

  private String comment;

  public ItemDTO() {

  }

  public ItemDTO(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getClusterId() {
    return clusterId;
  }

  public void setClusterId(long clusterId) {
    this.clusterId = clusterId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
