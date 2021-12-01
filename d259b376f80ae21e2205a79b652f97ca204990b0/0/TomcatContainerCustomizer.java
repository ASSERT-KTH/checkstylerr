package com.ctrip.apollo.biz.customize;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class TomcatContainerCustomizer implements EmbeddedServletContainerCustomizer {
  private static final String TOMCAT_ACCEPTOR_COUNT = "server.tomcat.acceptor-count";
  @Autowired
  private Environment environment;

  @Override
  public void customize(ConfigurableEmbeddedServletContainer container) {
    if (!(container instanceof TomcatEmbeddedServletContainerFactory)) {
      return;
    }
    if (!environment.containsProperty(TOMCAT_ACCEPTOR_COUNT)) {
      return;
    }
    TomcatEmbeddedServletContainerFactory tomcat =
        (TomcatEmbeddedServletContainerFactory) container;
    tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
      @Override
      public void customize(Connector connector) {
        ProtocolHandler handler = connector.getProtocolHandler();
        if (handler instanceof Http11NioProtocol) {
          Http11NioProtocol http = (Http11NioProtocol) handler;
          http.setBacklog(Integer.parseInt(environment.getProperty(TOMCAT_ACCEPTOR_COUNT)));
        }

      }
    });
  }
}
