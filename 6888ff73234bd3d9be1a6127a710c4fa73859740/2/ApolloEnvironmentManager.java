package com.ctrip.apollo.client;

import com.ctrip.apollo.client.loader.ConfigLoaderFactory;
import com.ctrip.apollo.client.loader.ConfigLoaderManager;
import com.ctrip.apollo.client.model.PropertySourceReloadResult;
import com.ctrip.apollo.client.util.ConfigUtil;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
class ApolloEnvironmentManager {
  private static final Logger logger = LoggerFactory.getLogger(ApolloEnvironmentManager.class);
  private ConfigLoaderManager configLoaderManager;
  private ConfigUtil configUtil;
  private ScheduledExecutorService executorService;
  private ApolloEnvironment apolloEnvironment;

  private AtomicBoolean initDone;

  ApolloEnvironmentManager(ApolloEnvironment apolloEnvironment) {
    this.apolloEnvironment = apolloEnvironment;
    this.configLoaderManager = ConfigLoaderFactory.getInstance().getConfigLoaderManager();
    this.configUtil = ConfigUtil.getInstance();
    this.executorService =
        Executors
            .newScheduledThreadPool(1,
                ApolloThreadFactory.create("ApolloEnvironmentManager", true));
    this.initDone = new AtomicBoolean();
  }

  synchronized void init() {
    if (initDone.get()) {
      logger.warn("ApolloEnvironmentManager init already done");
      return;
    }

    this.apolloEnvironment.updatePropertySource(this.configLoaderManager.loadPropertySource());
    this.schedulePeriodicRefresh();
    initDone.set(true);
  }

  void schedulePeriodicRefresh() {
    logger.info("Schedule periodic refresh with interval: {} {}",
        configUtil.getRefreshInterval(), configUtil.getRefreshTimeUnit());
    this.executorService.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              updatePropertySource();
            } catch (Throwable ex) {
              logger.error("Refreshing config failed", ex);
            }
          }
        }, configUtil.getRefreshInterval(), configUtil.getRefreshInterval(),
        configUtil.getRefreshTimeUnit());
  }

  void updatePropertySource() {
    PropertySourceReloadResult result = this.configLoaderManager.reloadPropertySource();
    if (result.hasChanges()) {
      logger.info("Found changes, refresh environment.");
      this.apolloEnvironment.updatePropertySource(result.getPropertySource(), result.getChanges());
    }
  }

}
