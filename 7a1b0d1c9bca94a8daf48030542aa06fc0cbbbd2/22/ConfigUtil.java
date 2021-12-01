package com.ctrip.apollo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigUtil {
  //TODO read from config?
  private static final int refreshInterval = 5;
  private static final TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;

  private static ConfigUtil configUtil = new ConfigUtil();

  private ConfigUtil() {
  }

  public static ConfigUtil getInstance() {
    return configUtil;
  }

  public String getAppId() {
    // TODO return the actual app id
    return "100003171";
  }

  public String getCluster() {
    // TODO return the actual cluster
    return "default";
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public TimeUnit getRefreshTimeUnit() {
    return refreshIntervalTimeUnit;
  }
}
