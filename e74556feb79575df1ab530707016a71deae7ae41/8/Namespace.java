package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "Update Namespace set isDeleted = 'false' where id = ?")
@Where(clause = "isDeleted = 'false'")
public class Namespace extends BaseEntity {

  @Column(nullable = false)
  private String appId;

  @Column(nullable = false)
  private String clusterName;

  @Column(nullable = false)
  private String namespaceName;

  public String getAppId() {
    return appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

}
