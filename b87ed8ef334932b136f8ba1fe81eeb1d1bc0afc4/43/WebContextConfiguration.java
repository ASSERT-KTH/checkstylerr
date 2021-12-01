package com.ctrip.framework.apollo.portal.configuration;

import com.google.common.base.Strings;

import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.filters.RecordAccessUserFilter;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Configuration
@Profile("ctrip")
public class WebContextConfiguration {

  @Autowired
  private ServerConfigService serverConfigService;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @Bean
  public ServletContextInitializer servletContextInitializer() {

    return new ServletContextInitializer() {

      @Override
      public void onStartup(ServletContext servletContext) throws ServletException {
        String loggingServerIP = serverConfigService.getValue("loggingServerIP");
        String loggingServerPort = serverConfigService.getValue("loggingServerPort");
        String credisServiceUrl = serverConfigService.getValue("credisServiceUrl");
        servletContext.setInitParameter("loggingServerIP",
            Strings.isNullOrEmpty(loggingServerIP) ? "" : loggingServerIP);
        servletContext.setInitParameter("loggingServerPort",
            Strings.isNullOrEmpty(loggingServerPort) ? "" : loggingServerPort);
        servletContext.setInitParameter("credisServiceUrl",
            Strings.isNullOrEmpty(credisServiceUrl) ? "" : credisServiceUrl);
      }
    };
  }

  @Bean
  public FilterRegistrationBean recordAccessUserFilter() {
    FilterRegistrationBean filter = new FilterRegistrationBean();
    filter.setFilter(new RecordAccessUserFilter(userInfoHolder));
    filter.addUrlPatterns("/apps");
    return filter;
  }

}
