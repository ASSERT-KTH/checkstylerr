package com.hsp.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class MyAroundAdvice implements MethodInterceptor{

	@Override
	public Object invoke(MethodInvocation arg0) throws Throwable {
		// TODO Auto-generated method stub
		System.out.println("****************************");
	
		
		System.out.println("调用方法前");
//这个就是 调用的方法
		Object obj = arg0.proceed();
		System.out.println("****************************");

		System.out.println("调用方法后");
		return obj;
	}

}
