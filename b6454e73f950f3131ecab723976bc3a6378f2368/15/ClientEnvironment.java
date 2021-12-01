package com.ctrip.apollo.client.env;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.Apollo.Env;
import com.ctrip.apollo.client.constants.Constants;
import com.ctrip.apollo.core.MetaDomainConsts;
import com.ctrip.apollo.core.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
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
      apolloProperties = readConfigFile(DEFAULT_FILE, null);
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

  @SuppressWarnings("unchecked")
  private Properties readConfigFile(String configPath, Properties defaults) {
    InputStream in = this.getClass().getResourceAsStream(configPath);
    logger.info("Reading config from resource {}", configPath);
    Properties props = new Properties();
    try {
      if (in == null) {
        // load outside resource under current user path
        Path path = new File(System.getProperty("user.dir") + configPath).toPath();
        if (Files.isReadable(path)) {
          in = new FileInputStream(path.toFile());
          logger.info("Reading config from file {} ", path);
        }
      }
      if (defaults != null) {
        props.putAll(defaults);
      }

      if (in != null) {
        props.load(in);
        in.close();
      }
    } catch (Exception ex) {
      logger.warn("Reading config failed: {}", ex.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ex) {
          logger.warn("Close config failed: {}", ex.getMessage());
        }
      }
    }
    StringBuilder sb = new StringBuilder();
    for (Enumeration<String> e = (Enumeration<String>) props.propertyNames(); e
        .hasMoreElements(); ) {
      String key = e.nextElement();
      String val = (String) props.getProperty(key);
      sb.append(key).append('=').append(val).append('\n');
    }
    logger.info("Reading properties: \n" + sb.toString());
    return props;
  }
}
