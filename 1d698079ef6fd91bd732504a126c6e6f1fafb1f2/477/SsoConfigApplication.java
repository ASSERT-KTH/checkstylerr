package com.carl.auth.ssoconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class SsoConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsoConfigApplication.class, args);
	}
}