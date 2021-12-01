package com.ctrip.apollo.util;

import com.google.common.base.Preconditions;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.env.ClientEnvironment;

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
    String appId = ClientEnvironment.getAppId();
    Preconditions.checkState(appId != null, "app.id is not set");
    return appId;
  }

  /**
   * Get the cluster name for the current application.
   * @return the cluster name, or "default" if not specified
   */
  public String getCluster() {
    String cluster = ClientEnvironment.getCluster();
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
    Env env = ClientEnvironment.getEnv();
    Preconditions.checkState(env != null, "env is not set");
    return env;
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
