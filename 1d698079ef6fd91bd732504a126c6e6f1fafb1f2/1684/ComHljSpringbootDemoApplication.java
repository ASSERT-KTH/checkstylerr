package com.hlj.springboot.dome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@ImportResource(value = "classpath:applicationContext.xml")
@EnableJpaRepositories(basePackages = {"com.hlj.springboot.dome.common.entity.repository"})
@EntityScan(basePackages = "com.hlj.springboot.dome.common.entity")
public class ComHljSpringbootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComHljSpringbootDemoApplication.class, args);
	}
}
