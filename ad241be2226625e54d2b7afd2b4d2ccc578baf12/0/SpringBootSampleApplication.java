package com.ctrip.framework.apollo.demo.spring;

import com.ctrip.framework.apollo.demo.spring.config.SampleRedisConfig;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * @author Jason Song
 */
@SpringBootApplication
public class SpringBootSampleApplication {
  @Bean
  public SampleRedisConfig sampleRedisConfig() {
    return new SampleRedisConfig();
  }

  public static void main(String[] args) {
    new SpringApplicationBuilder(SpringBootSampleApplication.class).run(args);
    onKeyExit();
  }

  private static void onKeyExit() {
    System.out.println("Press Enter to exit...");
    new Scanner(System.in).nextLine();
  }
}
