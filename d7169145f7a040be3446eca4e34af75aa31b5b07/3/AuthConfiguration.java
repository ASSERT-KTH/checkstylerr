package com.ctrip.framework.apollo.portal.configutation;

import com.ctrip.framework.apollo.portal.auth.CtripUserInfoHolder;
import com.ctrip.framework.apollo.portal.auth.NotCtripUserInfoHolder;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
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
  static class CtripProfileConfiguration{

    @Autowired
    private ServerConfigRepository serverConfigRepository;

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
      filterInitParam.put("serverName", serverConfigRepository.findByKey("serverName").getValue());
      filterInitParam.put("casServerLoginUrl", serverConfigRepository.findByKey("casServerLoginUrl").getValue());

      casFilter.setInitParameters(filterInitParam);
      casFilter.setFilter(filter("org.jasig.cas.client.authentication.AuthenticationFilter"));
      casFilter.addUrlPatterns("/*");

      return casFilter;
    }

    @Bean
    public FilterRegistrationBean casValidationFilter(){
      FilterRegistrationBean casValidationFilter = new FilterRegistrationBean();
      Map<String, String> filterInitParam = new HashMap();
      filterInitParam.put("casServerUrlPrefix", serverConfigRepository.findByKey("casServerUrlPrefix").getValue());
      filterInitParam.put("serverName", serverConfigRepository.findByKey("serverName").getValue());
      filterInitParam.put("encoding", "UTF-8");
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

      assertionHolderFilter.setFilter(filter("org.jasig.cas.client.util.AssertionThreadLocalFilter"));
      assertionHolderFilter.addUrlPatterns("/*");

      return assertionHolderFilter;
    }

    @Bean
    public CtripUserInfoHolder ctripUserInfoHolder(){
      return new CtripUserInfoHolder();
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
  }

  /**
   * 默认实现
   */
  @Configuration
  static class NotCtripProfileConfiguration{

    @Bean
    public NotCtripUserInfoHolder notCtripUserInfoHolder(){
      return new NotCtripUserInfoHolder();
    }
  }


}
