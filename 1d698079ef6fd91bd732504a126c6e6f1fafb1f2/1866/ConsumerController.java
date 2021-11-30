package com.didispace.web;

import com.didispace.dto.User;
import com.didispace.service.RefactorHelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//这里用来测试对feign客户端的调用
@RestController
public class ConsumerController {

    @Autowired
    RefactorHelloService refactorHelloService;

    /**
     *  2、测试传参
     */

    @RequestMapping(value = "/feign-consumer2", method = RequestMethod.GET)
    public String helloConsumer2() {
        StringBuilder sb = new StringBuilder();
        sb.append(refactorHelloService.hello("DIDI")).append("\n");
        sb.append(refactorHelloService.hello("DIDI", 30)).append("\n");
        sb.append(refactorHelloService.hello(new User("DIDI", 30))).append("\n");
        return sb.toString();
    }
}