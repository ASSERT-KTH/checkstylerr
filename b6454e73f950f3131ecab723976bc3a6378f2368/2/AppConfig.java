package com.ctrip.apollo.demo;

import com.ctrip.apollo.client.ApolloConfigManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
@ComponentScan(value = "com.ctrip.apollo.demo")
public class AppConfig {
  @Bean
  public ApolloConfigManager apolloConfigManager() {
    return new ApolloConfigManager();
  }

//    @Bean
//    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }
//
//    @Bean
//    public static RefreshScope refreshScope() {
//        return new RefreshScope();
//    }
}
