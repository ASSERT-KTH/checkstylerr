package com.ctrip.framework.apollo.portal.api;


import org.springframework.beans.factory.annotation.Autowired;

public class API {

  @Autowired
  protected RetryableRestTemplate restTemplate;

}
