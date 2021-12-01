package com.ctrip.framework.apollo.demo.spring.springBootDemo.refresh;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.demo.spring.common.refresh.ApolloRefreshConfig;
import com.ctrip.framework.apollo.demo.spring.springBootDemo.config.SampleRedisConfig;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class SpringBootApolloRefreshConfig implements ConfigChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(SpringBootApolloRefreshConfig.class);

  @Autowired
  private ApolloRefreshConfig apolloRefreshConfig;

  @Autowired
  private SampleRedisConfig sampleRedisConfig;

  @ApolloConfig
  private Config config;

  @Autowired
  private RefreshScope refreshScope;

  @PostConstruct
  private void init() {
    config.addChangeListener(this);
  }

  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    logger.info("sampleRedisConfig before refresh {}", sampleRedisConfig.toString());
    refreshScope.refresh("sampleRedisConfig");
    logger.info("sampleRedisConfig after refresh {}", sampleRedisConfig.toString());
  }
}
