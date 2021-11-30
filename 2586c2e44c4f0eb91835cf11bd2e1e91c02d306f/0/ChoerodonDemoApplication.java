package me.flyleft.choerodon.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class ChoerodonDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChoerodonDemoApplication.class, args);
    }

}
