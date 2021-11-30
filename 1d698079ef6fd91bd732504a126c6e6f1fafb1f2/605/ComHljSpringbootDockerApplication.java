package com.hlj.spring.boot.docker;

import com.hlj.spring.boot.docker.utils.DateHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

@SpringBootApplication
public class ComHljSpringbootDockerApplication {

	public static void main(String[] args) {
		System.out.println("________________________________________");

		System.out.println(		DateHelper.convertDate2String(new Date(),DateHelper.YYYY_MM_DD_HH_MM_SS));
		System.out.println(new Date().toString());

		System.out.println("________________________________________");

		SpringApplication.run(ComHljSpringbootDockerApplication.class, args);
	}
}
