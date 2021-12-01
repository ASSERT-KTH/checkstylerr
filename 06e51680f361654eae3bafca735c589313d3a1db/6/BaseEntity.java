package com.ctrip.apollo.biz.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Where(clause = "isDeleted = 0")
@SQLDelete(sql = "Update Cluster set isDeleted = 1 where id = ?")
public abstract class BaseEntity {

  @Id
  @GeneratedValue
  private long id;

  private boolean isDeleted;

  @Column(name = "DataChange_CreatedBy")
  private String dataChangeCreatedBy;

  @Column(name = "DataChange_CreatedTime")
  private Date dataChangeCreatedTime;

  @Column(name = "DataChange_LastModifiedBy")
  private String dataChangeLastModifiedBy;

  @Column(name = "DataChange_LastTime")
  private Date dataChangeLastModifiedTime;

  public String getDataChangeCreatedBy() {
    return dataChangeCreatedBy;
  }

  public Date getDataChangeCreatedTime() {
    return dataChangeCreatedTime;
  }

  public String getDataChangeLastModifiedBy() {
    return dataChangeLastModifiedBy;
  }

  public Date getDataChangeLastModifiedTime() {
    return dataChangeLastModifiedTime;
  }

  public long getId() {
    return id;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDataChangeCreatedBy(String dataChangeCreatedBy) {
    this.dataChangeCreatedBy = dataChangeCreatedBy;
  }

  public void setDataChangeCreatedTime(Date dataChangeCreatedTime) {
    this.dataChangeCreatedTime = dataChangeCreatedTime;
  }

  public void setDataChangeLastModifiedBy(String dataChangeLastModifiedBy) {
    this.dataChangeLastModifiedBy = dataChangeLastModifiedBy;
  }

  public void setDataChangeLastModifiedTime(Date dataChangeLastModifiedTime) {
    this.dataChangeLastModifiedTime = dataChangeLastModifiedTime;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }

  public void setId(long id) {
    this.id = id;
  }
}
