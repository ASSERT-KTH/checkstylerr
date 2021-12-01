package com.ctrip.framework.apollo.portal.configutation;

import com.ctrip.framework.apollo.portal.auth.CtripLogoutHandler;
import com.ctrip.framework.apollo.portal.auth.CtripUserInfoHolder;
import com.ctrip.framework.apollo.portal.auth.CtripUserService;
import com.ctrip.framework.apollo.portal.auth.DefaultLogoutHandler;
import com.ctrip.framework.apollo.portal.auth.DefaultUserInfoHolder;
import com.ctrip.framework.apollo.portal.auth.DefaultUserService;
import com.ctrip.framework.apollo.portal.auth.LogoutHandler;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;
import com.ctrip.framework.apollo.portal.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;


/**
 * sso相关的配置.
 */
@Configuration
public class AuthConfiguration {

  /**
   * 在ctrip内部运行时,会指定 spring.profiles.active = ctrip.
   * ctrip sso是通过cas实现的,所以需要加载相关的filter和listener.
   */
  @Configuration
  @Profile("ctrip")
  static class CtripAuthAutoConfiguration {

    @Autowired
    private ServerConfigService serverConfigService;

    @Bean
    public ServletListenerRegistrationBean redisAppSettingListner(){
      ServletListenerRegistrationBean redisAppSettingListner = new ServletListenerRegistrationBean();
      redisAppSettingListner.setListener(listener("org.jasig.cas.client.credis.CRedisAppSettingListner"));
      return redisAppSettingListner;
    }

    @Bean
    public ServletListenerRegistrationBean singleSignOutHttpSessionListener(){
      ServletListenerRegistrationBean singleSignOutHttpSessionListener = new ServletListenerRegistrationBean();
      singleSignOutHttpSessionListener.setListener(listener("org.jasig.cas.client.session.SingleSignOutHttpSessionListener"));
      return singleSignOutHttpSessionListener;
    }

    @Bean
    public FilterRegistrationBean casFilter(){
      FilterRegistrationBean singleSignOutFilter = new FilterRegistrationBean();
      singleSignOutFilter.setFilter(filter("org.jasig.cas.client.session.SingleSignOutFilter"));
      singleSignOutFilter.addUrlPatterns("/*");
      return singleSignOutFilter;
    }

    @Bean
    public FilterRegistrationBean authenticationFilter(){
      FilterRegistrationBean casFilter = new FilterRegistrationBean();

      Map<String, String> filterInitParam = new HashMap();
      filterInitParam.put("redisClusterName", "casClientPrincipal");
      filterInitParam.put("serverName", serverConfigService.getValue("serverName"));
      filterInitParam.put("casServerLoginUrl", serverConfigService.getValue("casServerLoginUrl"));
      //we don't want to use session to store login information, since we will be deployed to a cluster, not a single instance
      filterInitParam.put("useSession", "false");

      casFilter.setInitParameters(filterInitParam);
      casFilter.setFilter(filter("com.ctrip.framework.apollo.sso.filter.ApolloAuthenticationFilter"));
      casFilter.addUrlPatterns("/*");

      return casFilter;
    }

    @Bean
    public FilterRegistrationBean casValidationFilter(){
      FilterRegistrationBean casValidationFilter = new FilterRegistrationBean();
      Map<String, String> filterInitParam = new HashMap();
      filterInitParam.put("casServerUrlPrefix", serverConfigService.getValue("casServerUrlPrefix"));
      filterInitParam.put("serverName", serverConfigService.getValue("serverName"));
      filterInitParam.put("encoding", "UTF-8");
      //we don't want to use session to store login information, since we will be deployed to a cluster, not a single instance
      filterInitParam.put("useSession", "false");
      filterInitParam.put("useRedis", "true");
      filterInitParam.put("redisClusterName", "casClientPrincipal");

      casValidationFilter.setFilter(filter("org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter"));
      casValidationFilter.setInitParameters(filterInitParam);
      casValidationFilter.addUrlPatterns("/*");

      return casValidationFilter;

    }



    @Bean
    public FilterRegistrationBean assertionHolder(){
      FilterRegistrationBean assertionHolderFilter = new FilterRegistrationBean();

      assertionHolderFilter.setFilter(filter("com.ctrip.framework.apollo.sso.filter.ApolloAssertionThreadLocalFilter"));
      assertionHolderFilter.addUrlPatterns("/*");

      return assertionHolderFilter;
    }

    @Bean
    public CtripUserInfoHolder ctripUserInfoHolder(){
      return new CtripUserInfoHolder();
    }

    @Bean
    public CtripLogoutHandler logoutHandler(){
      return new CtripLogoutHandler();
    }

    private Filter filter(String className){
      Class clazz = null;
      try {
        clazz = Class.forName(className);
        Object obj = clazz.newInstance();
        return (Filter) obj;
      } catch (Exception e) {
        throw new RuntimeException("instance filter fail", e);
      }

    }

    private EventListener listener(String className){
      Class clazz = null;
      try {
        clazz = Class.forName(className);
        Object obj = clazz.newInstance();
        return (EventListener) obj;
      } catch (Exception e) {
        throw new RuntimeException("instance listener fail", e);
      }
    }

    @Bean
    public UserService ctripUserService(ServerConfigService serverConfigService) {
      return new CtripUserService(serverConfigService);
    }
  }

  /**
   * 默认实现
   */
  @Configuration
  static class DefaultAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UserInfoHolder.class)
    public DefaultUserInfoHolder notCtripUserInfoHolder(){
      return new DefaultUserInfoHolder();
    }

    @Bean
    @ConditionalOnMissingBean(LogoutHandler.class)
    public DefaultLogoutHandler logoutHandler(){
      return new DefaultLogoutHandler();
    }

    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService defaultUserService() {
      return new DefaultUserService();
    }
  }


}
