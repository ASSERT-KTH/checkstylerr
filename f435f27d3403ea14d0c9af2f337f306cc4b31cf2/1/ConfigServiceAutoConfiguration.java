package com.ctrip.framework.apollo.configservice;

import com.ctrip.framework.apollo.biz.message.ReleaseMessageScanner;
import com.ctrip.framework.apollo.configservice.controller.ConfigFileController;
import com.ctrip.framework.apollo.configservice.controller.NotificationController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
public class ConfigServiceAutoConfiguration {
  @Autowired
  private NotificationController notificationController;
  @Autowired
  private ConfigFileController configFileController;

  @Bean
  public ReleaseMessageScanner releaseMessageScanner() {
    ReleaseMessageScanner releaseMessageScanner = new ReleaseMessageScanner();
    //handle server cache first
    releaseMessageScanner.addMessageListener(configFileController);
    releaseMessageScanner.addMessageListener(notificationController);
    return releaseMessageScanner;
  }

}
