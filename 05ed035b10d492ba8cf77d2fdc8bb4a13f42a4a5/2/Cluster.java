package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
public class Cluster extends BaseEntity{
  
  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String appId;

  @Column
  private long clusterVersionId;
  
  public Cluster() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public long getClusterVersionId() {
    return clusterVersionId;
  }

  public void setClusterVersionId(long clusterVersionId) {
    this.clusterVersionId = clusterVersionId;
  }

}
