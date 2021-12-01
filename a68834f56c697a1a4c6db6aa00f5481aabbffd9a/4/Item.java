package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;

@Entity
@SQLDelete(sql = "Update Item set isDeleted = 1 where id = ?")
public class Item extends BaseEntity {

  @Column(nullable = false)
  private long groupId;

  @Column(nullable = false)
  private String key;

  @Column
  private String value;

  @Column
  private String comment;

  public long getGroupId() {
    return groupId;
  }

  public void setGroupId(long groupId) {
    this.groupId = groupId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

}
