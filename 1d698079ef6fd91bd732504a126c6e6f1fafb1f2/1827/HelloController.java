package com.didispace.web;

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

	/**
	 * 1、断路器
	 * @return
	 */
//	@RequestMapping(value = "/hello", method = RequestMethod.GET)
//	public String hello() {
//
//		ServiceInstance instance = client.getLocalServiceInstance();
//		logger.info("/hello, host:" + instance.getHost() + ", service_id:" + instance.getServiceId());
//		return "Hello World";
//	}


	/**
	 * 2、断路器，模拟服务阻塞
	 */
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() throws InterruptedException {

		ServiceInstance instance = client.getLocalServiceInstance();


		int sleepTime = new Random().nextInt(3000);
		logger.info("处理线程等待 "+sleepTime+" 秒");
		Thread.sleep(sleepTime);

		logger.info("/hello, host:" + instance.getHost() + ", service_id:" + instance.getServiceId());
		return "Hello World";
	}

}