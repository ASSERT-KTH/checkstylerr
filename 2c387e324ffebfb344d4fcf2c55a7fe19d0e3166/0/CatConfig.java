package com.ctrip.apollo.common.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dianping.cat.servlet.CatFilter;
import com.dianping.cat.servlet.CatListener;

@Configuration
public class CatConfig {

  @Bean
  public CatFilter catFilter() {
    return new CatFilter();
  }

  @Bean
  public CatListener catListener() {
    return new CatListener();
  }
}
