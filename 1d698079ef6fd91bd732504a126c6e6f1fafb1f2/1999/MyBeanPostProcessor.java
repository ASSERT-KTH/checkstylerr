package com.hsp.beanlife;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MyBeanPostProcessor implements BeanPostProcessor {

	public Object postProcessAfterInitialization(Object arg0, String arg1)
			throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("postProcessAfterInitialization 这个方法被调用");
		System.out.println(arg0+" 被创建的时间是 "+new java.util.Date());
		return arg0;
	}
	
//这个先运行
	public Object postProcessBeforeInitialization(Object arg0, String arg1)
			throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("postProcessBeforeInitialization 这个方法被调用");
		System.out.println(arg0+" 被创建的时间是 "+new java.util.Date());

		return arg0;
	}

}
