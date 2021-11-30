package com.didispace.web;

import com.didispace.domain.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
public class HelloController {

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	private DiscoveryClient client;

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {

		ServiceInstance instance = client.getLocalServiceInstance();
		logger.info("/hello, host:" + instance.getHost() + ", service_id:" + instance.getServiceId());
		return "Hello World";
	}


	@RequestMapping(value = "/hello1", method = RequestMethod.GET)
	String hello(@RequestParam("name") String name) {

		return "hello" +name;
	}

	@RequestMapping(value = "/hello2", method = RequestMethod.GET)
	User hello(@RequestHeader("name") String name, @RequestHeader("age") Integer age){
		return  new User(name,age);
	}



	@RequestMapping(value = "/hello3", method = RequestMethod.POST)
	String hello(@RequestBody User user){
		return "hello"+user.getName()+","+user.getAge();
	}


	@RequestMapping(value = "/api-a/hello", method = RequestMethod.GET)
	public String api_a() {
		ServiceInstance instance = client.getLocalServiceInstance();
		logger.info("/hello, host:" + instance.getHost() + ", service_id:" + instance.getServiceId());
		return "Hello World";
	}


}