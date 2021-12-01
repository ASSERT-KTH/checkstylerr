package com.ctrip.apollo.biz.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Entity
public class Version extends BaseEntity {
  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String clusterId;

  // parent version could be null
  @Column
  private Long parentVersion;

  private List<Release> releases;

  public Version() {}

  public String getClusterId() {
    return clusterId;
  }

  public long getId() {
    return id;
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

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public void setId(long id) {
    this.id = id;
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
