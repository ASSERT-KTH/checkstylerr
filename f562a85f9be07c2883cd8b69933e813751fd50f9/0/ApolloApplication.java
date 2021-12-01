package com.ctrip.apollo.assembly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;

import com.ctrip.apollo.adminservice.AdminServiceApplication;
import com.ctrip.apollo.configservice.ConfigServiceApplication;
import com.ctrip.apollo.portal.PortalApplication;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
public class ApolloApplication {

  private static final Logger logger = LoggerFactory.getLogger(ApolloApplication.class);

  public static void main(String[] args) throws Exception {
    /**
     * Common
     */
    ConfigurableApplicationContext commonContext =
        new SpringApplicationBuilder(ApolloApplication.class).web(false).run(args);
    commonContext.addApplicationListener(new ApplicationPidFileWriter());
    logger.info(commonContext.getId() + " isActive: " + commonContext.isActive());

    /**
     * ConfigService
     */
    if (commonContext.getEnvironment().containsProperty("configservice")) {
      ConfigurableApplicationContext configContext =
          new SpringApplicationBuilder(ConfigServiceApplication.class).parent(commonContext)
              .sources(RefreshScope.class).run(args);
      logger.info(configContext.getId() + " isActive: " + configContext.isActive());
    }

    /**
     * AdminService
     */
    if (commonContext.getEnvironment().containsProperty("adminservice")) {
      ConfigurableApplicationContext adminContext =
          new SpringApplicationBuilder(AdminServiceApplication.class).parent(commonContext)
              .sources(RefreshScope.class).run(args);
      logger.info(adminContext.getId() + " isActive: " + adminContext.isActive());
    }

    /**
     * Portal
     * 
     * WARNING: Do not start portal at the same pool as configservice, they will conflict on 8080
     */
    if (commonContext.getEnvironment().containsProperty("portal")) {
      ConfigurableApplicationContext portalContext =
          new SpringApplicationBuilder(PortalApplication.class).parent(commonContext).run(args);
      logger.info(portalContext.getId() + " isActive: " + portalContext.isActive());
    }
  }

}
