package com.ctrip.apollo.common.auth;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {
  
  private RestTemplate restTemplate;

  public RestTemplate getObject() {
    return restTemplate;
  }

  public Class<RestTemplate> getObjectType() {
    return RestTemplate.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() {
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials("apollo", ""));
    CloseableHttpClient httpClient =
        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

    restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
  }

}
