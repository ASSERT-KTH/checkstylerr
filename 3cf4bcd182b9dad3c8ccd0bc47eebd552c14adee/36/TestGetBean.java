package com.mashibing.spring;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestGetBean {

	/**
	 * Spring �Զ����������˵ һ��������
	 * 
	 * 1. ���� singleton -> ws request session application -> �������ڰ� 2. new������
	 * prototype
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");


//
//		try {
//			Car car = CarFactory.getCar("audi");
//			System.out.println(car.getName());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		
		Car car = ctx.getBean("car",Car.class);
		System.out.println(car.getName());

	}
}
