package com.ctrip.apollo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring boot application entry point
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@SpringBootApplication
@EnableEurekaServer
public class ServerApplication {

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext context =
        new SpringApplicationBuilder(ServerApplication.class).web(true).run(args);
  }

}
