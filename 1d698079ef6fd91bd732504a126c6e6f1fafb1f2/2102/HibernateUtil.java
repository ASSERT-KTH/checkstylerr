package com.hlj.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

final public class HibernateUtil {
	private static SessionFactory sessionFactory=null;
	//使用线程局部模式 
	private static ThreadLocal<Session> threadLocal=new ThreadLocal<Session>();
	//不然随便实力话
	private HibernateUtil(){};
	static {
		sessionFactory=new Configuration().configure("com/hlj/config/hibernate.cfg.xml").buildSessionFactory();
	}
	
	//获取全新的全新的sesession
	public static Session openSession(){
		return sessionFactory.openSession();
	}
	
	
	
	//获取和线程关联的session
	public static Session getCurrentSession(){
		
		Session session=threadLocal.get();
		//判断是否得到了session 如果没有得到就开始 让他得到
		if(session==null){
			session=sessionFactory.openSession();
			//把session对象设置到 threadLocal,相当于该session已经和线程绑定
//这样就可以不要cfg 中的配置了
// 	<property name="current_session_context_class">thread</property>	
			threadLocal.set(session);
		}
		return session;		
	}
}
