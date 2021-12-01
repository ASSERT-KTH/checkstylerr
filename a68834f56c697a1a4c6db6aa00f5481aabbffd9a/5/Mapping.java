package com.ctrip.apollo.biz.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.SQLDelete;

@Entity
@SQLDelete(sql = "Update Mapping set isDeleted = 1 where id = ?")
public class Mapping extends BaseEntity {

  private long versionId;

  private long releaseId;

  public long getReleaseId() {
    return releaseId;
  }

  public long getVersionId() {
    return versionId;
  }

  public void setReleaseId(long releaseId) {
    this.releaseId = releaseId;
  }

  public void setVersionId(long versionId) {
    this.versionId = versionId;
  }
}
