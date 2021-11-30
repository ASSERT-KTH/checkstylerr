package com.didispace.web;

import com.didispace.dto.User;
import com.didispace.service.HelloService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefactorHelloController implements HelloService{


    public String hello(@RequestParam("name") String name) {

        return "hello" +name;
    }

    public User hello(@RequestHeader("name") String name, @RequestHeader("age") Integer age){
        return  new User(name,age);
    }


    public String hello(@RequestBody User user){
        return "hello"+user.getName()+","+user.getAge();
    }

}
