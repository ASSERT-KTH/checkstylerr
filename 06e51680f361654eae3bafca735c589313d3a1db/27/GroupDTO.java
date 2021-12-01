package com.ctrip.apollo.core.dto;

public class GroupDTO {

  private long id;
  
  private long clusterId;

  private long namespaceId;

  private String name;

  public long getClusterId() {
    return clusterId;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getNamespaceId() {
    return namespaceId;
  }

  public void setClusterId(long clusterId) {
    this.clusterId = clusterId;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }
}
