package com.didispace.web;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

//绑定服务提供者，并使用spirngMvc 注解绑定具体REST接口
@FeignClient(name="HELLO-SERVICE")
public interface HelloService {

    @RequestMapping("/hello")
    String hello();


}