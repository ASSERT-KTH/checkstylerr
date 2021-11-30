package com.hlj.ioc;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class App1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		//使用spring来完成上面的任务
		//得到spring 对象中的容器对象applicationContext.xml 
	//当我们实例化这个对象的时候 bean中的东西就会被实例化出来
	//通过类路径
		ApplicationContext ac = new ClassPathXmlApplicationContext("com/hlj/ioc/beans.xml");
			Student student = (Student)ac.getBean("student");
			Student student2 = (Student)ac.getBean("student");
			System.out.println(student+"\n"+student2);
		
	//通过文件路径 
	//ApplicationContext ac2=new FileSystemXmlApplicationContext("src\\com\\hlj\\ioc\\beans.xml");

		
		//�������ʹ��beanfactoryȥ��ȡbean������ֻ��ʵ��������� ��ô
		//������bean����ʵ��,ֻ�е���ȥʹ��getBeanĳ��beanʱ���Ż�ʵʱ�Ĵ���.
		
		BeanFactory factory = new XmlBeanFactory(new ClassPathResource("com/hlj/ioc/beans.xml"));
		factory.getBean("student");
		
		//�����չ��Ӳ��
	
	}

}
