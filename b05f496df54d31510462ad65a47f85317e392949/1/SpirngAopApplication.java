package com.healerjean.proj;

import com.healerjean.proj.service.UserService;
import com.healerjean.proj.service.impl.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class SpirngAopApplication {

    public static void main(String[] args) {
        // SpringApplication.run(SpirngAopApplication.class, args);


        //Spring 中的接口
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpirngAopApplication.class);
        UserService userService = annotationConfigApplicationContext.getBean(UserServiceImpl.class);
        userService.login(1L);

    }

}
