package com.ctrip.apollo.portal.entity;

import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.enums.Env;

import java.util.List;

public class AppInfoVO {

  private AppDTO app;
  /**
   * 在创建app的时候可能在某些环境下创建失败
   */
  private List<Env> missEnvs;

  public AppDTO getApp() {
    return app;
  }

  public void setApp(AppDTO app) {
    this.app = app;
  }

  public List<Env> getMissEnvs() {
    return missEnvs;
  }

  public void setMissEnvs(List<Env> missEnvs) {
    this.missEnvs = missEnvs;
  }
}
