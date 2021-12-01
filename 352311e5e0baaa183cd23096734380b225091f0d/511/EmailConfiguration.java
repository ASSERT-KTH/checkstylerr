/*
 * Copyright 2021 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.portal.spi.configuration;


import com.ctrip.framework.apollo.common.condition.ConditionalOnMissingProfile;
import com.ctrip.framework.apollo.portal.spi.EmailService;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripEmailService;
import com.ctrip.framework.apollo.portal.spi.ctrip.CtripEmailRequestBuilder;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultEmailService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EmailConfiguration {

  /**
   * spring.profiles.active = ctrip
   */
  @Configuration
  @Profile("ctrip")
  public static class CtripEmailConfiguration {

    @Bean
    public EmailService ctripEmailService() {
      return new CtripEmailService();
    }

    @Bean
    public CtripEmailRequestBuilder emailRequestBuilder() {
      return new CtripEmailRequestBuilder();
    }
  }

  /**
   * spring.profiles.active != ctrip
   */
  @Configuration
  @ConditionalOnMissingProfile({"ctrip"})
  public static class DefaultEmailConfiguration {
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService defaultEmailService() {
      return new DefaultEmailService();
    }
  }



}

