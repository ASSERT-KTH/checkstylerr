package com.hsp.aop;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;

public class MyMethodBeforeAdvice implements MethodBeforeAdvice {
 
	/**
	 * 
	 * method: 被调用的方法
	 * args: 这个是传给方法的参数有哪些
	 * target:  这个是目标对象
	 */
	public void before(Method method, Object[] args, Object target)
			throws Throwable {
		System.out.println("****************************");
		// TODO Auto-generated method stub
		System.out.println("得到的方法"+method.getName()+"参数"+args+"rarget"+target);

	}

}
