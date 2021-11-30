package com.eureka.server.one;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer  //开启Eureka服务注册中心的支持,用来供给其他应用进行对话
@SpringBootApplication
public class OneEurekasErverApplication {

	public static void main(String[] args) {
		SpringApplication.run(OneEurekasErverApplication.class, args);
	}
}
