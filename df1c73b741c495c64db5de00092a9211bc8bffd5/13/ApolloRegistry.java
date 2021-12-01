package com.ctrip.apollo.client.model;

import com.google.common.base.MoreObjects;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloRegistry {
  private String appId;
  private String version;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("appId", appId)
        .add("version", version)
        .toString();
  }
}
