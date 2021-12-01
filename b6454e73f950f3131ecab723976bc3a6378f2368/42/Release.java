package com.ctrip.apollo.biz.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@Where(clause = "isDeleted = 0")
@SQLDelete(sql = "Update Release set isDeleted = 1 where id = ?")
public class Release {
  @Id
  @GeneratedValue
  private long id;

  private String name;
  private long appId;
  private String comment;
  private boolean isDeleted;

  public Release() {
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

  public long getAppId() {
    return appId;
  }

  public void setAppId(long appId) {
    this.appId = appId;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }
}
