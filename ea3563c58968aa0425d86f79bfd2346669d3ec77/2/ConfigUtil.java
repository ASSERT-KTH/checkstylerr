package com.ctrip.framework.apollo.util;

import com.google.common.base.Preconditions;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.enums.EnvUtils;
import com.ctrip.framework.foundation.Foundation;

import org.unidal.lookup.annotation.Named;
import org.unidal.net.Networks;

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
   *
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
   *
   * @return the current data center, null if there is no such info.
   */
  public String getDataCenter() {
    String dataCenter = Foundation.server().getDataCenter();
    //TODO use sub env from framework foundation if data center is null
    return dataCenter;
  }

  /**
   * Get the cluster name for the current application.
   *
   * @return the cluster name, or "default" if not specified
   */
  public String getCluster() {
    //Load data center from system property
    String cluster = System.getProperty("apollo.cluster");

    //Use data center as cluster
    if (cluster == null) {
      cluster = getDataCenter();
    }

    //Use default cluster
    if (cluster == null) {
      cluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    }
    return cluster;
  }

  /**
   * Get the current environment.
   *
   * @return the env
   * @throws IllegalStateException if env is set
   */
  public Env getApolloEnv() {
    Env env = EnvUtils.transformEnv(Foundation.server().getEnvType());
    Preconditions.checkState(env != null, "env is not set");
    return env;
  }

  public String getLocalIp() {
    return Networks.forIp().getLocalHostAddress();
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
