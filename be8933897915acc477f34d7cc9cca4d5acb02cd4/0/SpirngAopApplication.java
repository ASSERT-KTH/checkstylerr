package com.healerjean.proj;

import com.healerjean.proj.service.UserService;
import com.healerjean.proj.service.impl.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 想要分析SpringBoot的启动过程，我们需要熟悉SpringApplication的构造过程以及SpringApplication的run方法执行过程即可。
 */
@SpringBootApplication
public class SpirngAopApplication {

    public static void main(String[] args) {
        // SpringApplication.run(SpirngAopApplication.class, args);

        //AnnotationConfigApplicationContext 是基于注解来使用的，它不需要配置文件，
        //采用 java 配置类和各种注解来配置，是比较简单的方式，也是大势所趋吧。
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpirngAopApplication.class);
        // 从 context 中取出我们的 Bean，而不是用 new UserService() 这种方式
        UserService userService = annotationConfigApplicationContext.getBean(UserService.class);

        userService.login(1L);

    }



}
