package com.ctrip.framework.apollo.common.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "AppNamespace")
@SQLDelete(sql = "Update AppNamespace set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class AppNamespace extends BaseEntity {

  @Column(name = "Name", nullable = false)
  private String name;

  @Column(name = "AppId", nullable = false)
  private String appId;

  @Column(name = "Comment")
  private String comment;

  public String getAppId() {
    return appId;
  }

  public String getComment() {
    return comment;
  }

  public String getName() {
    return name;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String toString() {
    return toStringHelper().add("name", name).add("appId", appId).add("comment", comment)
        .toString();
  }
}
