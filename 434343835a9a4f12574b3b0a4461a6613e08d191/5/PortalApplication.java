package com.ctrip.apollo.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@EnableAutoConfiguration
@Configuration
@PropertySource(value = {"classpath:portal.properties"})
@ComponentScan(basePackageClasses = {com.ctrip.apollo.common.ApolloCommonConfig.class,
    com.ctrip.apollo.portal.PortalApplication.class})
public class PortalApplication {

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext context = SpringApplication.run(PortalApplication.class, args);
    context.addApplicationListener(new ApplicationPidFileWriter());
    context.addApplicationListener(new EmbeddedServerPortFileWriter());
  }
}
