package com.ctrip.framework.apollo.util.factory;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.OrderedProperties;
import com.google.common.base.Strings;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default PropertiesFactory implementation.
 *
 * @author songdragon@zts.io
 */
public class DefaultPropertiesFactory implements PropertiesFactory {

  private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesFactory.class);

  private ConfigUtil m_configUtil;

  public DefaultPropertiesFactory() {
    m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
  }

  @Override
  public Properties getPropertiesInstance() {
    if (m_configUtil.isPropertiesOrderEnabled()) {
      return new OrderedProperties();
    } else {
      return new Properties();
    }
  }
}
