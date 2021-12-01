package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
public class Release  extends BaseEntity{
  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String name;
  
  @Column(nullable = false)
  private long groupId;
  
  @Column(nullable = false)
  @Lob
  private String configurations;
  
  @Column(nullable = false)
  private String comment;

  public Release() {
  }

  public long getGroupId() {
    return groupId;
  }

  public String getComment() {
    return comment;
  }

  public String getConfigurations() {
    return configurations;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setGroupId(long groupId) {
    this.groupId = groupId;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setConfigurations(String configurations) {
    this.configurations = configurations;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }
}
