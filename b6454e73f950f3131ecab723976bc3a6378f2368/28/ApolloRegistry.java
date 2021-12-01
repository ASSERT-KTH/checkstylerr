package com.ctrip.apollo.client.model;

import com.google.common.base.MoreObjects;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloRegistry {
  private long appId;
  private String version;

  public long getAppId() {
    return appId;
  }

  public void setAppId(long appId) {
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
