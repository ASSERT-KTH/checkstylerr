package com.ctrip.framework.apollo.biz.customize;

import com.google.common.base.Strings;

import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import com.ctrip.framework.apollo.biz.repository.ServerConfigRepository;
import com.ctrip.framework.foundation.Foundation;
import com.dianping.cat.Cat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.util.Objects;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class LoggingCustomizer implements InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(LoggingCustomizer.class);
  private static final String cLoggingAppenderClass =
      "com.ctrip.framework.clogging.agent.appender.CLoggingAppender";
  private static boolean cLoggingAppenderPresent =
      ClassUtils.isPresent(cLoggingAppenderClass, LoggingCustomizer.class.getClassLoader());

  private static final String CLOGGING_SERVER_URL_KEY = "clogging.server.url";
  private static final String CLOGGING_SERVER_PORT_KEY = "clogging.server.port";

  @Autowired
  private ServerConfigRepository serverConfigRepository;

  @Override
  public void afterPropertiesSet() {
    if (!cLoggingAppenderPresent) {
      return;
    }

    try {
      tryConfigCLogging();
    } catch (Throwable ex) {
      logger.error("Config CLogging failed", ex);
      Cat.logError(ex);
    }

  }

  private void tryConfigCLogging() throws Exception {
    String appId = Foundation.app().getAppId();
    if (Strings.isNullOrEmpty(appId)) {
      logger.warn("App id is null or empty!");
      return;
    }

    ServerConfig cloggingUrl = serverConfigRepository.findByKey(CLOGGING_SERVER_URL_KEY);
    ServerConfig cloggingPort = serverConfigRepository.findByKey(CLOGGING_SERVER_PORT_KEY);

    if (Objects.isNull(cloggingUrl) || Objects.isNull(cloggingPort)) {
      logger.warn("CLogging config is not set!");
      return;
    }

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Class clazz = Class.forName(cLoggingAppenderClass);
    Appender cLoggingAppender = (Appender) clazz.newInstance();

    ReflectionUtils.findMethod(clazz, "setAppId", String.class).invoke(cLoggingAppender, appId);
    ReflectionUtils.findMethod(clazz, "setServerIp", String.class)
        .invoke(cLoggingAppender, cloggingUrl.getValue());
    ReflectionUtils.findMethod(clazz, "setServerPort", int.class)
        .invoke(cLoggingAppender, Integer.parseInt(cloggingPort.getValue()));

    cLoggingAppender.setName("CentralLogging");
    cLoggingAppender.setContext(loggerContext);
    cLoggingAppender.start();

    ch.qos.logback.classic.Logger logger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
    logger.addAppender(cLoggingAppender);

  }

}
