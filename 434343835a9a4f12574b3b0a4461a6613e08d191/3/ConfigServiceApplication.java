package com.ctrip.apollo.configservice;

import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring boot application entry point
 *
 * @author Jason Song(song_s@ctrip.com)
 */

@EnableEurekaServer
@EnableAspectJAutoProxy
@EnableAutoConfiguration//(exclude = EurekaClientConfigBean.class)
@Configuration
@PropertySource(value = {"classpath:configservice.properties"})
@ComponentScan(basePackageClasses = {com.ctrip.apollo.common.ApolloCommonConfig.class,
    com.ctrip.apollo.biz.ApolloBizConfig.class,
    com.ctrip.apollo.configservice.ConfigServiceApplication.class})
public class ConfigServiceApplication {

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext context =
        new SpringApplicationBuilder(ConfigServiceApplication.class).run(args);
    context.addApplicationListener(new ApplicationPidFileWriter());
    context.addApplicationListener(new EmbeddedServerPortFileWriter());
  }

}
