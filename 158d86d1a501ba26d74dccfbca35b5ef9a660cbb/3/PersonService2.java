package com.hlj.beanlife;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
public class PersonService2 implements BeanNameAware,BeanFactoryAware {

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
	public PersonService2(String abc){
		System.out.println(" 构造函数 PersonService String abc");
	}
	
	public PersonService2(){
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
	//这个bean的工厂是 
	public void setBeanFactory(BeanFactory arg0) throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("这个bean需要的工厂是  "+arg0);

	}

	//�÷������Դ���beanFactroy
/*	public void setBeanFactory(BeanFactory arg0) throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("setBeanFactory "+arg0);
	}*/
	//�÷�������ApplicationContext
/*	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		System.out.println("setApplicationContext"+arg0);
		
	}*/

	//Ҳ����ͨ��ע��ķ�ʽ�������ĸ�����init-method
/*	public void init(){
		System.out.println("���Լ���init����");
	}
	
	//	
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("afterPropertiesSet()");
	}

	//�������ǵ���ٷ���
	@PreDestroy
	public  void mydestory(){
		System.out.println("�ͷŸ�����Դ");
	}*/


	
}
