package com.ctrip.apollo.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
@RefreshScope
public class DemoService {
  private String foo;

  @Value("${101.foo}")
  private void setFoo(String foo) {
    this.foo = foo;
  }

  public String getFoo() {
    return foo;
  }
}
