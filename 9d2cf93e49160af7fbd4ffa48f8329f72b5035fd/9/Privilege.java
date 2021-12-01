package com.ctrip.apollo.biz.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "Update Privilege set isDeleted = 'false' where id = ?")
@Where(clause = "isDeleted = 'false'")
public class Privilege extends BaseEntity {

  @Column
  private String name;

  @Column
  private String privilType;

  @Column
  private long namespaceId;

  public String getName() {
    return name;
  }

  public long getNamespaceId() {
    return namespaceId;
  }

  public String getPrivilType() {
    return privilType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }

  public void setPrivilType(String privilType) {
    this.privilType = privilType;
  }
}
