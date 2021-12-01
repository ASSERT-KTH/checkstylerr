package com.ctrip.apollo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.core.utils.ResourceUtils;

public class MetaDomainConsts {

  private static Map<Env, String> domains = new HashMap<>();

  static {
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);
    domains.put(Env.LOCAL, prop.getProperty("local.meta", "http://localhost:8080"));
    domains.put(Env.DEV, prop.getProperty("dev.meta"));
    domains.put(Env.FAT, prop.getProperty("fat.meta"));
    domains.put(Env.FWS, prop.getProperty("fws.meta"));
    domains.put(Env.UAT, prop.getProperty("uat.meta"));
    domains.put(Env.LPT, prop.getProperty("lpt.meta"));
    domains.put(Env.TOOLS, prop.getProperty("tools.meta"));
    domains.put(Env.PRO, prop.getProperty("pro.meta"));
  }

  public static String getDomain(Env env) {
    return domains.get(env);
  }
}
