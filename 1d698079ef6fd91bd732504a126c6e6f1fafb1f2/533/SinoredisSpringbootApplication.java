package com.hlj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.redis.core.RedisTemplate;

@ImportResource(value = "classpath:applicationContext.xml")
@SpringBootApplication
public class SinoredisSpringbootApplication{


	public static void main(String[] args) {
		SpringApplication.run(SinoredisSpringbootApplication.class, args);
	}

}
