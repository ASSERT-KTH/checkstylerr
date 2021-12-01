package com.ctrip.framework.apollo.portal.configutation;

import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Configuration
@Profile("ctrip")
public class ServletContextConfiguration {

  @Autowired
  private ServerConfigRepository serverConfigRepository;

  @Bean
  public ServletContextInitializer initializer(){

    return new ServletContextInitializer() {

      @Override
      public void onStartup(ServletContext servletContext) throws ServletException {
        ServerConfig loggingServerIP = serverConfigRepository.findByKey("loggingServerIP");
        ServerConfig loggingServerPort = serverConfigRepository.findByKey("loggingServerPort");
        ServerConfig credisServiceUrl = serverConfigRepository.findByKey("credisServiceUrl");
        servletContext.setInitParameter("loggingServerIP", loggingServerIP == null ? "" :loggingServerIP.getValue());
        servletContext.setInitParameter("loggingServerPort", loggingServerPort == null ? "" :loggingServerPort.getValue());
        servletContext.setInitParameter("credisServiceUrl", credisServiceUrl == null ? "" :credisServiceUrl.getValue());
      }
    };
  }

}
