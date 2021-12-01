package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "Update Item set isDeleted = 'false' where id = ?")
@Where(clause = "isDeleted = 'false'")
public class Item extends BaseEntity {

  @Column(nullable = false)
  private long namespaceId;

  @Column(nullable = false)
  private String key;

  @Column
  private String value;

  @Column
  private String comment;

  public String getComment() {
    return comment;
  }

  public String getKey() {
    return key;
  }

  public long getNamespaceId() {
    return namespaceId;
  }

  public String getValue() {
    return value;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
