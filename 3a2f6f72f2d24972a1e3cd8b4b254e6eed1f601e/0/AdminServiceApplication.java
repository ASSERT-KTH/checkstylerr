package com.ctrip.apollo.adminservice;

import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.actuate.system.EmbeddedServerPortFileWriter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

@EnableAspectJAutoProxy
@EnableEurekaClient
@Configuration
@PropertySource(value = {"classpath:adminservice.properties"})
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {com.ctrip.apollo.common.ApolloCommonConfig.class,
    com.ctrip.apollo.biz.ApolloBizConfig.class,
    com.ctrip.apollo.adminservice.AdminServiceApplication.class})
public class AdminServiceApplication {
  public static void main(String[] args) {
    ConfigurableApplicationContext context =
        new SpringApplicationBuilder(AdminServiceApplication.class).run(args);
    context.addApplicationListener(new ApplicationPidFileWriter());
    context.addApplicationListener(new EmbeddedServerPortFileWriter());
  }
}
