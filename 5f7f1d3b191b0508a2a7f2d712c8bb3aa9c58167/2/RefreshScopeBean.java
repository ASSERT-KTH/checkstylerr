package com.ctrip.framework.apollo.demo.spring.common.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This bean is annotated with @RefreshScope so that it can be 'refreshed' at runtime
 * @author Jason Song(song_s@ctrip.com)
 */
@RefreshScope
@Component("refreshScopeBean")
public class RefreshScopeBean {
  private static final Logger logger = LoggerFactory.getLogger(RefreshScopeBean.class);

  @Value("${timeout:200}")
  private int timeout;
  @Value("${batch:100}")
  private int batch;

  /**
   * this method will be called when the bean is first initialized, and when it is 'refreshed' as well
   */
  @PostConstruct
  void initialize() {
    logger.info("timeout is {}", timeout);
    logger.info("batch is {}", batch);
  }

  @Override
  public String toString() {
    return String.format("[RefreshScopeBean] timeout: %d, batch: %d", timeout, batch);
  }
}
