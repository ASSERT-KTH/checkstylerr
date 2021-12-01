package com.ctrip.apollo.biz.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.SQLDelete;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
@SQLDelete(sql = "Update Version set isDeleted = 1 where id = ?")
public class Version extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private long clusterId;

  // parent version could be null
  @Column
  private Long parentVersionId;

  @ManyToMany
  private List<Release> releases; 
  
  public long getClusterId() {
    return clusterId;
  }

  public String getName() {
    return name;
  }

  public Long getParentVersionId() {
    return parentVersionId;
  }

  public List<Release> getReleases() {
    return releases;
  }


  public void setClusterId(long clusterId) {
    this.clusterId = clusterId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setParentVersionId(Long parentVersionId) {
    this.parentVersionId = parentVersionId;
  }

  public void setReleases(List<Release> releases) {
    this.releases = releases;
  }

}
