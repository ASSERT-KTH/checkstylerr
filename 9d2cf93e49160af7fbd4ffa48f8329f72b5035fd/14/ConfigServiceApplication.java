package com.ctrip.apollo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Spring boot application entry point
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@SpringBootApplication
@EnableEurekaServer
public class ConfigServiceApplication {

  public static void main(String[] args) throws Exception {
    new SpringApplicationBuilder(ConfigServiceApplication.class).run(args);
  }

}
