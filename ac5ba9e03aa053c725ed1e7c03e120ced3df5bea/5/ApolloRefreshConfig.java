package com.ctrip.framework.apollo.demo.spring.common.refresh;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.demo.spring.common.bean.RefreshScopeBean;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * To refresh the config bean when config is changed
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class ApolloRefreshConfig implements ConfigChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(ApolloRefreshConfig.class);

  @ApolloConfig
  private Config config;
  @ApolloConfig("FX.apollo")
  private Config anotherConfig;
  @Autowired
  private RefreshScope refreshScope;

  @Autowired
  @Qualifier("refreshScopeBean")
  private RefreshScopeBean refreshScopeBean;

  @PostConstruct
  private void init() {
    logger.info(refreshScopeBean.toString());
    config.addChangeListener(this);
    anotherConfig.addChangeListener(this);
  }

  @Override
  public void onChange(ConfigChangeEvent changeEvent) {
    logger.info("refreshScopeBean before refresh {}", refreshScopeBean.toString());
    //could also call refreshScope.refreshAll();
    refreshScope.refresh("refreshScopeBean");
    logger.info("refreshScopeBean after refresh {}", refreshScopeBean.toString());
  }
}
