package com.ctrip.apollo.client.util;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import com.ctrip.apollo.client.constants.Constants;
import com.ctrip.apollo.client.model.ApolloRegistry;
import com.ctrip.apollo.core.utils.ClassLoaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigUtil {
  private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
  public static final String APOLLO_PROPERTY = "apollo.properties";
  //TODO read from config?
  private static final int refreshInterval = 5;
  private static final TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;

  private static ConfigUtil configUtil = new ConfigUtil();

  private ConfigUtil() {
  }

  public static ConfigUtil getInstance() {
    return configUtil;
  }

  public String getCluster() {
    // TODO return the actual cluster
    return "default";
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public TimeUnit getRefreshTimeUnit() {
    return refreshIntervalTimeUnit;
  }

  public List<ApolloRegistry> loadApolloRegistries() throws IOException {
    List<URL> resourceUrls =
        Collections.list(ClassLoaderUtil.getLoader().getResources(APOLLO_PROPERTY));
    List<ApolloRegistry> registries =
        FluentIterable.from(resourceUrls).transform(new Function<URL, ApolloRegistry>() {
          @Override
          public ApolloRegistry apply(URL input) {
            Properties properties = loadPropertiesFromResourceURL(input);
            if (properties == null || !properties.containsKey(Constants.APP_ID)) {
              return null;
            }
            ApolloRegistry registry = new ApolloRegistry();
            registry.setAppId(properties.getProperty(Constants.APP_ID));
            registry.setVersion(
                properties.getProperty(Constants.VERSION, Constants.DEFAULT_VERSION_NAME));
            return registry;
          }
        }).filter(Predicates.notNull()).toList();
    return registries;
  }

  Properties loadPropertiesFromResourceURL(URL resourceUrl) {
    try {
      InputStream inputStream = resourceUrl.openStream();
      Properties prop = new Properties();
      prop.load(inputStream);
      return prop;
    } catch (IOException ex) {
      logger.error("Load properties from {} failed", resourceUrl.toExternalForm(), ex);
    }
    return null;
  }
}
