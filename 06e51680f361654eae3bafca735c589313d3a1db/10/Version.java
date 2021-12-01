package com.ctrip.apollo.biz.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
public class Version extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private long clusterId;

  // parent version could be null
  @Column
  private Long parentVersion;

  @ManyToMany
  private List<Release> releases; 
  
  public long getClusterId() {
    return clusterId;
  }

  public String getName() {
    return name;
  }

  public Long getParentVersion() {
    return parentVersion;
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

  public void setParentVersion(Long parentVersion) {
    this.parentVersion = parentVersion;
  }

  public void setReleases(List<Release> releases) {
    this.releases = releases;
  }

}
