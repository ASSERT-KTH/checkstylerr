package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "Update AppNamespace set isDeleted = 'false' where id = ?")
@Where(clause = "isDeleted = 'false'")
public class AppNamespace extends BaseEntity{

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String appId;

  @Column
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

}
