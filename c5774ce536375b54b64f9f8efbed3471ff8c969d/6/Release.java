package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@SQLDelete(sql = "Update Release set isDeleted = 'false' where id = ?")
@Where(clause = "isDeleted = 0")
public class Release extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String appId;

  @Column(nullable = false)
  private String clusterName;

  @Column
  private String namespaceName;

  @Column(nullable = false)
  @Lob
  private String configurations;

  @Column(nullable = false)
  private String comment;

  public String getAppId() {
    return appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getComment() {
    return comment;
  }

  public String getConfigurations() {
    return configurations;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public String getName() {
    return name;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setConfigurations(String configurations) {
    this.configurations = configurations;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public void setName(String name) {
    this.name = name;
  }

}
