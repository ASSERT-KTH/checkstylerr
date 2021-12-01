package com.ctrip.apollo.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.framework.foundation.Foundation;

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

  /**
   * Get the app id for the current application.
   * @return the app id
   * @throws IllegalStateException if app id is not set
   */
  public String getAppId() {
    String appId = Foundation.app().getAppId();
    Preconditions.checkState(appId != null, "app.id is not set");
    return appId;
  }

  /**
   * Get the data center info for the current application.
   * @return the current data center, null if there is no such info.
   */
  public String getDataCenter() {
    return Foundation.server().getDataCenter();
  }

  /**
   * Get the cluster name for the current application.
   * @return the cluster name, or "default" if not specified
   */
  public String getCluster() {
    String cluster = System.getProperty("apollo.cluster");
    if (cluster == null) {
      cluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
    return cluster;
  }

  /**
   * Get the current environment.
   * @return the env
   * @throws IllegalStateException if env is set
   */
  public Env getApolloEnv() {
    Env env = transformEnv(Foundation.server().getEnvType());
    Preconditions.checkState(env != null, "env is not set");
    return env;
  }

  private Env transformEnv(String envName) {
    if (Strings.isNullOrEmpty(envName)) {
      return null;
    }
    switch (envName.toUpperCase()) {
      case "LPT":
        return Env.LPT;
      case "FAT":
      case "FWS":
        return Env.FAT;
      case "UAT":
        return Env.UAT;
      case "PRO":
        return Env.PRO;
      case "DEV":
        return Env.DEV;
      case "LOCAL":
        return Env.LOCAL;
      default:
        return null;
    }
  }

  public String getMetaServerDomainName() {
    return MetaDomainConsts.getDomain(getApolloEnv());
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
