package com.ctrip.apollo.client.env;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.client.constants.Constants;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.utils.ResourceUtils;
import com.ctrip.apollo.core.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class ClientEnvironment {

  private static final Logger logger = LoggerFactory.getLogger(ClientEnvironment.class);

  private static final String DEFAULT_FILE = "/apollo.properties";

  private AtomicReference<Env> env = new AtomicReference<Env>();

  private static ClientEnvironment instance = new ClientEnvironment();

  private ClientEnvironment() {

  }

  public static ClientEnvironment getInstance() {
    return instance;
  }

  public Env getEnv() {
    if (env.get() == null) {
      Env resultEnv = Apollo.getEnv();
      Properties apolloProperties = null;
      apolloProperties = ResourceUtils.readConfigFile(DEFAULT_FILE, null);
      if (apolloProperties != null) {
        String strEnv = apolloProperties.getProperty(Constants.ENV);
        if (!StringUtils.isBlank(strEnv)) {
          resultEnv = Env.valueOf(strEnv.trim().toUpperCase());
        }
      }
      env.compareAndSet(null, resultEnv);
    }

    if (env.get() == null) {
      throw new IllegalArgumentException("Apollo env is not set");
    }

    return env.get();
  }

  public String getMetaServerDomainName() {
    return MetaDomainConsts.getDomain(getEnv());
  }

}
