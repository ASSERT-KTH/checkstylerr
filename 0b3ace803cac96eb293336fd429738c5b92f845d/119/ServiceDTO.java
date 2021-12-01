package com.ctrip.framework.apollo.core.dto;

import com.google.common.base.MoreObjects;

public class ServiceDTO {

  private String appName;

  private String instanceId;

  private String homepageUrl;

  public String getAppName() {
    return appName;
  }

  public String getHomepageUrl() {
    return homepageUrl;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setHomepageUrl(String homepageUrl) {
    this.homepageUrl = homepageUrl;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("appName", appName)
        .add("instanceId", instanceId)
        .add("homepageUrl", homepageUrl)
        .toString();
  }
}
