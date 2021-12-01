/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.samples.springmvc.consumer;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.samples.common.schema.Hello;
import io.servicecomb.samples.common.schema.models.Person;

@Component
public class SpringmvcConsumerMain {

  private static RestTemplate restTemplate = RestTemplateBuilder.create();

  @RpcReference(microserviceName = "springmvc", schemaId = "springmvcHello")
  private static Hello hello;

  public static void main(String[] args) throws Exception {
    init();
    Person person = new Person();
    person.setName("ServiceComb/Java Chassis");

    // RestTemplate Consumer or POJO Consumer. You can choose whatever you like
    // RestTemplate Consumer
    String sayHiResult =
        restTemplate.postForObject("cse://springmvc/springmvchello/sayhi?name=Java Chassis", null, String.class);
    String sayHelloResult = restTemplate.postForObject("cse://springmvc/springmvchello/sayhello", person, String.class);
    System.out.println("RestTemplate Consumer or POJO Consumer.  You can choose whatever you like.");
    System.out.println("RestTemplate consumer sayhi services: " + sayHiResult);
    System.out.println("RestTemplate consumer sayhello services: " + sayHelloResult);

    // POJO Consumer
    System.out.println("POJO consumer sayhi services: " + hello.sayHi("Java Chassis"));
    System.out.println("POJO consumer sayhi services: " + hello.sayHello(person));
  }

  public static void init() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }
}
