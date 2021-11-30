package com.hlj.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
//使用final 不能被实例化了
final public class MySessionFactory {

	private static SessionFactory sessionFactory=null;
	
	private static Configuration configuration=null;
	
	static{
		configuration=new Configuration().configure();
		sessionFactory=configuration.buildSessionFactory();
	}
	
	private MySessionFactory(){};
	
	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}
	
}
