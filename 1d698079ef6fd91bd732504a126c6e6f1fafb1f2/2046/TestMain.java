package com.hlj.view;

import java.util.Date;

import oracle.net.aso.s;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.hlj.domain.Employee;
import com.hlj.util.MySessionFactory;

public class TestMain implements updateEmployee {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	/*	addEmployee();
		updateEmployee();
		deleteEmployee();*/
	}

	private static void deleteEmployee() {
		//然后我们开始删除的操作了
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=session.beginTransaction();

		Employee employee =  (Employee)session.load(Employee.class, 2);
		session.delete(employee); 
		ts.commit();
		session.close();
	}

	private static void updateEmployee() {
		//现在开始修改数据库中的表的数据,和下面的东西是一样的但是这个需要占用很多的内存
		
		//所以我们将这个SessionFactory制作成单态的,通过制作一个工具类
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=session.beginTransaction();
		//修改用户Employee.java先获取用户，然后修改
		//load是可以通过主键来获取表的记录
		Employee employee =(Employee)session.load(Employee.class, 3);
		//下面这个就是相当于更新了	
		employee.setEmail("13151515@qq.com");
			ts.commit();
			session.close();
	}

	private static void addEmployee() {
		//对数据持久层操作 
		//1. 创建Configurateion该类主要是用于读取hibernate.cfg.xml
		Configuration cf=new Configuration().configure();
		//2. 创建一个会话工厂.是一个重量级的对象，占用内存大
		SessionFactory sf=cf.buildSessionFactory();
		//3. 创建Session(会话)<->Connection
		Session session=sf.openSession();
		//4.没有下面事务，下面的东西是提交不了，也就是保存不到数据中的 ，
		//开始事务 
		Transaction ts=session.beginTransaction();
		//加入一个对象(记录)Employee.java
		Employee u=new Employee();
		u.setEmail("1318830916@qq.com");
		u.setHiredate(new Date());
		u.setName("zhangyujin");
		//insert 
		//保存，
		
//这个就是持久化对象了
		session.save(u);//添加该对象到数据库  //等于==inset into 。。被hibernate封装，将来不管用什么数据库都是可以试实现的
		ts.commit();//提交事务
		session.close();
	}

}

