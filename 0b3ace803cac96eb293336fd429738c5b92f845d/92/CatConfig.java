package com.ctrip.framework.apollo.common.controller;

import javax.servlet.DispatcherType;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dianping.cat.servlet.CatFilter;
import com.dianping.cat.servlet.CatListener;

@Configuration
public class CatConfig {

  @Bean
  public FilterRegistrationBean catFilter() {
    FilterRegistrationBean bean = new FilterRegistrationBean();
    bean.setFilter(new CatFilter());
    bean.setName("cat-filter");
    bean.addUrlPatterns("/*");
    bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
    return bean;
  }

  @Bean
  public ServletListenerRegistrationBean<CatListener> catListener() {
    ServletListenerRegistrationBean<CatListener> bean =
        new ServletListenerRegistrationBean<CatListener>(new CatListener());
    bean.setName("cat-listener");
    return bean;
  }
}
