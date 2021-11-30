package com.hsp.aop;

import org.springframework.aop.ThrowsAdvice;
import org.springframework.asm.commons.Method;

public class MyThowAdvoce implements ThrowsAdvice{
	


	
	public void afterThrowing(Method m,Object[] os,Object target,Exception throwable){
		
		 System.out.println(m+"出大事了"+throwable.getMessage()); 
		
	}

}
