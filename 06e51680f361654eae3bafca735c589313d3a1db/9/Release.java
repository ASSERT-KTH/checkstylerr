package com.ctrip.apollo.biz.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
public class Release  extends BaseEntity{

  @Column(nullable = false)
  private String name;
  
  @Column(nullable = false)
  private long groupId;
  
  @Column
  private String groupName;
  
  @Column(nullable = false)
  @Lob
  private String configurations;
  
  @Column(nullable = false)
  private String comment;

  @ManyToMany
  private List<Version> versions;
  
  @Column(nullable=false)
  private String appId;
  
  @Column(nullable=false)
  private String clusterName;
  
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

  public long getGroupId() {
    return groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getName() {
    return name;
  }

  public List<Version> getVersions() {
    return versions;
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

  public void setGroupId(long groupId) {
    this.groupId = groupId;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setVersions(List<Version> versions) {
    this.versions = versions;
  }
}
