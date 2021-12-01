/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.rest.config;

import org.apache.metron.rest.MetronRestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.web.client.RestTemplate;

import static org.apache.metron.rest.MetronRestConstants.TEST_PROFILE;

@Configuration
@Profile("!" + TEST_PROFILE)
public class RestTemplateConfig {

    private Environment environment;

    @Autowired
    public RestTemplateConfig(Environment environment) {
      this.environment = environment;
    }

    @Bean
    public RestTemplate restTemplate() {
        if (environment.getProperty(MetronRestConstants.KERBEROS_ENABLED_SPRING_PROPERTY, Boolean.class, false)) {
            String keyTabLocation = environment.getProperty(MetronRestConstants.KERBEROS_KEYTAB_SPRING_PROPERTY);
            String userPrincipal = environment.getProperty(MetronRestConstants.KERBEROS_PRINCIPLE_SPRING_PROPERTY);
            return new KerberosRestTemplate(keyTabLocation, userPrincipal);
        } else {
            return new RestTemplate();
        }

    }

}
