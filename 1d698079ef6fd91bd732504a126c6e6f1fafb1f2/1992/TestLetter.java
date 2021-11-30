package com.hlj.inter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hlj.service.BybService;
import com.hlj.service.UserService;
import com.util.ApplicaionContextUtil;

public class TestLetter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

 
		//使用spring来完成上面的任务
		//得到spring 对象中的容器对象applicationContext.xml
 
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/hlj/inter/beans.xml");
	//不用接口 
/*		UpperLetter uLetter = (UpperLetter) ac.getBean("ChangeLetter");
		System.out.println(uLetter.change());*/
	//使用接口来访问 
		ChangeLetter changeLetter = (ChangeLetter) ac.getBean("ChangeLetter");
		System.out.println(changeLetter.change());

		
	}

}
