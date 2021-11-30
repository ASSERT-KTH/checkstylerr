package com.hlj.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

//��ʹ��hibernate������Ŀ����һ����ֻ֤��һ��SessionFactory
//һ����ݿ��Ӧһ��SessionFactory ����.
final public class MySessionFactory {

	private static SessionFactory sessionFactory=null;
	
	private MySessionFactory(){
		
	}
	
	static{
		
		sessionFactory =new Configuration().configure("com/hsp/config/hibernate.cfg.xml").buildSessionFactory(); 
		System.out.println("sessionFactory ����"+sessionFactory);
	}
	
	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}
	
}
