package com.hlj.springboot.dome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ComHljSpringbootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComHljSpringbootDemoApplication.class, args);
	}
}
