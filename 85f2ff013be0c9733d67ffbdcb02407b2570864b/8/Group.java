package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;

@Entity
@SQLDelete(sql = "Update Group set isDeleted = 1 where id = ?")
public class Group extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String appId;

  @Column(nullable = false)
  private long clusterId;

  @Column(nullable = false)
  private String clusterName;

  @Column(nullable = false)
  private long namespaceId;

  public String getAppId() {
    return appId;
  }

  public long getClusterId() {
    return clusterId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getName() {
    return name;
  }

  public long getNamespaceId() {
    return namespaceId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setClusterId(long clusterId) {
    this.clusterId = clusterId;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }

}
