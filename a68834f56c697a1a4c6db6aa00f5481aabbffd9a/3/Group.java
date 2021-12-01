package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;

@Entity
@SQLDelete(sql = "Update Group set isDeleted = 1 where id = ?")
public class Group extends BaseEntity {

  @Column(nullable = false)
  private long clusterId;

  @Column(nullable = false)
  private long namespaceId;

  @Column(nullable = false)
  private String name;

  public long getClusterId() {
    return clusterId;
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

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }

}
