package com.didispace.service;


import com.didispace.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
public class HelloServiceFallBack implements HelloService{

    public String hello(){
        return "error";
    }

    public String hello(@RequestParam("name") String name) {

        return "error";
    }

    public User hello(@RequestHeader("name") String name, @RequestHeader("age") Integer age){
        return new User("未知",0);
    }

    public String hello(@RequestBody User user){
        return "error";
    }
}
