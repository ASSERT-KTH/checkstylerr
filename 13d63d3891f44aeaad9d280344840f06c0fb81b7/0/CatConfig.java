package com.ctrip.framework.apollo.common.controller;

import javax.servlet.DispatcherType;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.dianping.cat.servlet.CatFilter;

@Configuration
public class CatConfig {

  @Bean
  public FilterRegistrationBean catFilter() {
    FilterRegistrationBean bean = new FilterRegistrationBean();
    bean.setFilter(new CatFilter());
    bean.setName("cat-filter");
    bean.addUrlPatterns("/*");
    bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }

}
