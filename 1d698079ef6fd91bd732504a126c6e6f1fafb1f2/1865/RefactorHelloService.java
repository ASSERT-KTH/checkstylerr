package com.didispace.service;

import com.didispace.service.HelloService;
import org.springframework.cloud.netflix.feign.FeignClient;

//绑定服务提供者
@FeignClient(name="HELLO-SERVICE")
public interface RefactorHelloService  extends HelloService {


}