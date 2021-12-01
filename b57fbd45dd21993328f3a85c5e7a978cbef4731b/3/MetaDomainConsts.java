package com.ctrip.apollo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.utils.ResourceUtils;

/**
 * The meta domain will load the meta server from System environment first, if not exist, will load
 * from apollo-env.properties. If neither exists, will load the default meta url.
 * 
 * Currently, apollo supports local/dev/fat/uat/lpt/pro environments.
 */
public class MetaDomainConsts {

  private static Map<Env, String> domains = new HashMap<>();

  public static final String DEFAULT_META_URL = "http://localhost:8080";

  static {
    Properties prop = new Properties();
    prop = ResourceUtils.readConfigFile("apollo-env.properties", prop);
    Map<String, String> env = System.getenv();
    domains.put(Env.LOCAL,
        env.getOrDefault("local_meta", prop.getProperty("local.meta", DEFAULT_META_URL)));
    domains.put(Env.DEV,
        env.getOrDefault("dev_meta", prop.getProperty("dev.meta", DEFAULT_META_URL)));
    domains.put(Env.FAT,
        env.getOrDefault("fat_meta", prop.getProperty("fat.meta", DEFAULT_META_URL)));
    domains.put(Env.UAT,
        env.getOrDefault("uat_meta", prop.getProperty("uat.meta", DEFAULT_META_URL)));
    domains.put(Env.LPT,
        env.getOrDefault("lpt_meta", prop.getProperty("lpt.meta", DEFAULT_META_URL)));
    domains.put(Env.PRO,
        env.getOrDefault("pro_meta", prop.getProperty("pro.meta", DEFAULT_META_URL)));
  }

  public static String getDomain(Env env) {
    return domains.get(env);
  }
}
