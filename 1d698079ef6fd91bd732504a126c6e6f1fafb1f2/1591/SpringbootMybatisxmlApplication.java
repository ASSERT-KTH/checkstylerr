package com.hlj.mybatisxml;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hlj.mybatisxml.mapper")
public class SpringbootMybatisxmlApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootMybatisxmlApplication.class, args);
	}
}
