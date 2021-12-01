package com.ctrip.apollo.core.dto;


import com.ctrip.apollo.core.enums.Env;

import java.util.LinkedList;
import java.util.List;

public class AppConfigVO {

  private String appId;

  private Env env;

  /**
   * latest version if version is zero, or is release version
   */
  private long versionId;

  /**
   * default cluster and app self’s configs
   */
  private List<ItemDTO> defaultClusterConfigs;

  /**
   * default cluster and override other app configs
   */
  private List<OverrideAppConfig> overrideAppConfigs;

  /**
   * configs in different cluster maybe different.
   * overrideClusterConfigs only save diff configs from default cluster.
   * For example:
   * default cluster has 3 configs:
   * {a -> A, b -> B, c -> C}
   *
   * cluster1 has 1 config
   * {b -> D}
   *
   * if client read cluster1 configs will return {a -> A, b -> D, c -> C}
   */
  private List<OverrideClusterConfig> overrideClusterConfigs;

  public AppConfigVO() {

  }

  public static AppConfigVO newInstance(String appId, long versionId) {
    AppConfigVO instance = new AppConfigVO();
    instance.setAppId(appId);
    instance.setVersionId(versionId);
    instance.setDefaultClusterConfigs(new LinkedList<>());
    instance.setOverrideAppConfigs(new LinkedList<>());
    instance.setOverrideClusterConfigs(new LinkedList<>());
    return instance;
  }

  public boolean isLatestVersion() {
    return versionId == 0;
  }

  public static class OverrideAppConfig {

    private String appId;
    private List<ItemDTO> configs;

    public OverrideAppConfig() {

    }

    public String getAppId() {
      return appId;
    }

    public void setAppId(String appId) {
      this.appId = appId;
    }

    public List<ItemDTO> getConfigs() {
      return configs;
    }

    public void setConfigs(List<ItemDTO> configs) {
      this.configs = configs;
    }

    public void addConfig(ItemDTO config) {
      if (configs == null) {
        configs = new LinkedList<>();
      }
      configs.add(config);
    }
  }


  public static class OverrideClusterConfig {

    private String clusterName;
    private List<ItemDTO> configs;

    public OverrideClusterConfig() {
    }

    public String getClusterName() {
      return clusterName;
    }

    public void setClusterName(String clusterName) {
      this.clusterName = clusterName;
    }

    public List<ItemDTO> getConfigs() {
      return configs;
    }

    public void setConfigs(List<ItemDTO> configs) {
      this.configs = configs;
    }
  }


  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Env getEnv() {
    return env;
  }

  public void setEnv(Env env) {
    this.env = env;
  }

  public long getVersionId() {
    return versionId;
  }

  public void setVersionId(long versionId) {
    this.versionId = versionId;
  }

  public List<ItemDTO> getDefaultClusterConfigs() {
    return defaultClusterConfigs;
  }

  public void setDefaultClusterConfigs(List<ItemDTO> defaultClusterConfigs) {
    this.defaultClusterConfigs = defaultClusterConfigs;
  }

  public List<OverrideAppConfig> getOverrideAppConfigs() {
    return overrideAppConfigs;
  }

  public void setOverrideAppConfigs(List<OverrideAppConfig> overrideAppConfigs) {
    this.overrideAppConfigs = overrideAppConfigs;
  }

  public List<OverrideClusterConfig> getOverrideClusterConfigs() {
    return overrideClusterConfigs;
  }

  public void setOverrideClusterConfigs(List<OverrideClusterConfig> overrideClusterConfigs) {
    this.overrideClusterConfigs = overrideClusterConfigs;
  }

  @Override
  public String toString() {
    return "Config4PortalDTO{" +
        "appId=" + appId +
        ", env=" + env +
        ", versionId=" + versionId +
        ", defaultClusterConfigs=" + defaultClusterConfigs +
        ", overrideAppConfigs=" + overrideAppConfigs +
        ", overrideClusterConfigs=" + overrideClusterConfigs +
        '}';
  }
}
