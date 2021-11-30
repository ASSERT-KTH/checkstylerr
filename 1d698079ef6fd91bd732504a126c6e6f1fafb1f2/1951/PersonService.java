package com.hsp.beanlife;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
//implements BeanNameAware,BeanFactoryAware,ApplicationContextAware,InitializingBean
public class PersonService implements BeanNameAware,BeanFactoryAware,ApplicationContextAware,InitializingBean,DisposableBean  {

	private String name;
	private Integer age;

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}
//这个构造函数的方法被使用的时候 就得 在配置中进行配置构造的方法 进行选择	
	public PersonService(String abc){
		System.out.println(" 构造函数 PersonService String abc");
	}
	
	public PersonService(){
		System.out.println("无参构造函数 PersonService ");
	}
 
	public void setName(String name) {
		System.out.println("这是一个set函数 setName(String name) ");
		this.name = name;
	}
	
	public void sayHi(){
		System.out.println("hi "+ name);
	}
 
	//表示正在被实例化的id是多少
	public void setBeanName(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("setBeanName String正在被实例化的id是  "+arg0);
		
	}

	@Override
	//这个bean的工厂是，不止是这个bean ，凡是被实例化的都会显示 
	public void setBeanFactory(BeanFactory arg0) throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("这个bean需要的工厂是  "+arg0);

	}

	@Override
	//返回的上下文 
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		System.out.println("这个项目的上下文  "+arg0);

		
		
		
		// TODO Auto-generated method stub
		
	}

	@Override
//在设置属性之后，执行，相当于是后置处理器处理之后，没有立刻执行处理器的after方法，而是执行下面这个方法
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println("这个是InitializingBean的方法");

		
	}

//自己定义的中间的方法
	public void zhangInit(){
		System.out.println("这个是我自己的方法");
	}
//自己定义的
	public void mydestory(){
		
		System.out.println("这个是我自己的销毁方法 释放各种资源，也许看不到");
	}

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("这个是接口定义的 销毁方法也许看不到");

	}




	
}
