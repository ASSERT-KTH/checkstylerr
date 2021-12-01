package com.ctrip.apollo.util;

import org.unidal.lookup.annotation.Named;

import java.util.concurrent.TimeUnit;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Named(type = ConfigUtil.class)
public class ConfigUtil {
  //TODO read from config?
  private static final int refreshInterval = 5;
  private static final TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;
  private static final int connectTimeout = 5000; //5 seconds
  private static final int readTimeout = 10000; //10 seconds

  public String getAppId() {
    // TODO return the actual app id
    return "100003171";
  }

  public String getCluster() {
    // TODO return the actual cluster
    return "default";
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public TimeUnit getRefreshTimeUnit() {
    return refreshIntervalTimeUnit;
  }
}
