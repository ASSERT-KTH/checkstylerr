package com.ctrip.framework.apollo.demo.spring.bean;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class AnnotatedBean {
  private static final Logger logger = LoggerFactory.getLogger(AnnotatedBean.class);

  @Value("${timeout:200}")
  private int timeout;
  private int batch;

  @ApolloConfig
  private Config config;
  @ApolloConfig("FX.apollo")
  private Config anotherConfig;

  @PostConstruct
  void initialize() {
    logger.info("timeout is {}", timeout);
    logger.info("batch is {}", batch);

    logger.info("Keys for config: {}", config.getPropertyNames());
    logger.info("Keys for anotherConfig: {}", anotherConfig.getPropertyNames());
  }

  @Value("${batch:100}")
  public void setBatch(int batch) {
    this.batch = batch;
  }

  @ApolloConfigChangeListener("application")
  private void someChangeHandler(ConfigChangeEvent changeEvent) {
    logger.info("[someChangeHandler]Changes for namespace {}", changeEvent.getNamespace());
    if (changeEvent.isChanged("timeout")) {
      refreshTimeout();
    }
  }

  @ApolloConfigChangeListener({"application", "FX.apollo"})
  private void anotherChangeHandler(ConfigChangeEvent changeEvent) {
    logger.info("[anotherChangeHandler]Changes for namespace {}", changeEvent.getNamespace());
    for (String key : changeEvent.changedKeys()) {
      ConfigChange change = changeEvent.getChange(key);
      logger.info("[anotherChangeHandler]Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
          change.getPropertyName(), change.getOldValue(), change.getNewValue(),
          change.getChangeType());
    }
  }

  private void refreshTimeout() {
    //do some custom logic to update placeholder value
    timeout = config.getIntProperty("timeout", timeout);
    logger.info("Refreshing timeout to {}", timeout);
  }
}
