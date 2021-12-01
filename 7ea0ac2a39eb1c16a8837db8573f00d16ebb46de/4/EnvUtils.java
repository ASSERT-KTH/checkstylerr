package com.ctrip.apollo.core.enums;

import com.ctrip.apollo.core.utils.StringUtils;

public final class EnvUtils {
  
  public static Env transformEnv(String envName) {
    if (StringUtils.isBlank(envName)) {
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
}
