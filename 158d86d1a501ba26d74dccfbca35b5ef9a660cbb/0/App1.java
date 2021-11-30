package com.hlj.beanlife;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class App1 {

	
	public static void main(String[] args) {

		ApplicationContext ac=new ClassPathXmlApplicationContext("com/hlj/beanlife/beans.xml");
		
		PersonService ps=(PersonService)ac.getBean("personService");
		ps.sayHi();
		
	}

}
