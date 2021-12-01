package com.ctrip.framework.apollo.mockserver;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;

/**
 * Create by zhangzheng on 8/23/18
 * Email:zhangzheng@youzan.com
 */
public class MockedMetaServerProvider implements MetaServerProvider{

  private static String address;

  public static void setAddress(String addr){
    address = addr;
  }

  @Override
  public String getMetaServerAddress(Env targetEnv) {
    return address;
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
