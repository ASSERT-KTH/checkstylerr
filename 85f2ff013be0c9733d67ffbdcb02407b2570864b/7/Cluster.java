package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@SQLDelete(sql = "Update Cluster set isDeleted = 1 where id = ?")
public class Cluster extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String appId;

  public String getAppId() {
    return appId;
  }

  public String getName() {
    return name;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setName(String name) {
    this.name = name;
  }

}
