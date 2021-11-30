package com.hlj.view;

import java.util.Date;
import java.util.List;

import javax.management.RuntimeErrorException;

import oracle.net.aso.s;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;

import com.hlj.domain.Employee;
import com.hlj.util.HibernateUtil;
import com.hlj.util.MySessionFactory;

public class TestMain implements updateEmployee {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*addEmployee();
		
		deleteEmployee();
		updateEmployee();
		deleteEmployee2();
		updateEmployee2();
		addEmployee2();
		//测试我们自己定义id assign
		addEmployee3();
		//测试我们的cfg.xml有没有切换位置
		addEmployee4();
		
		testCurrentSession();
		
		testgetAndload();
		testHibernateUtil();
		testQuery();*/
		testCriteria();
	}
	
	private static void testCriteria(){
		Session session=HibernateUtil.getCurrentSession();
		Transaction ts=null;
		
		try {
			
			ts=session.beginTransaction();
			
			Criteria cri=session.createCriteria(Employee.class).
			setMaxResults(2).addOrder(Order.asc("id") );
	//上面是去取出两天记录  设置 从降序排列出来 .desc("id")
			List<Employee> list=cri.list(); 
			for(Employee e: list){
				System.out.println(e.getId());
			}
			
			ts.commit();
			
		} catch (Exception e) {
			
			if(ts!=null){
				ts.rollback();
			}
			throw new RuntimeException(e.getMessage());
		}finally{
			//关闭session
			if(session!=null&&session.isOpen()){
				session.close();
			}
			
		}
	
	}
	
	
	private static void testQuery(){
		Session session = HibernateUtil.getCurrentSession(); 
	Transaction ts = null;
		try { 
			ts = session.beginTransaction();

			//获取query引用[这里 Employee不是表.而是domain类名]
			//[where 后面的条件可以是类的属性名，也可以是表的字段,安照hibernate规定，我们还是应该使用类的属性名.]
	Query query=session.createQuery("from Employee where id='6'");
	//通过list方法获取结果,这个list会自动的将封装成对应的domain对象
			//所以我们jdbc进行二次封装的工作没有.
	List<Employee> list=query.list();
			for(Employee e: list){ 
				System.out.println(e.getId()+" "+e.getHiredate());
			}
			
			ts.commit();
			
		} catch (Exception e) {
			
			if(ts!=null){
				ts.rollback();
			}
			throw new RuntimeException(e.getMessage());
		}finally{
			//关闭session
			if(session!=null&&session.isOpen()){
				session.close();
			}
			
		}


		
		
	}
	
	private static void testHibernateUtil(){
		Session session = HibernateUtil.getCurrentSession(); 
		Session session2 = HibernateUtil.getCurrentSession(); 

		/*System.out.println(employee); */

		System.out.println(session.hashCode()+"   "+session2.hashCode()); 
		
	}
	
	private static void testgetAndload(){
		Session session = MySessionFactory.getSessionFactory().openSession();
	//这里强调  100 是不存在
		Employee employee =  (Employee)session.load(Employee.class,100);
		Employee employee2 =  (Employee)session.get(Employee.class,1000);
		
		
		/*System.out.println(employee); */

		System.out.println(employee2); 
		
	}
	private static void testLoad(){
		Session session = MySessionFactory.getSessionFactory().openSession();
		//下面这个执行之后会 将这个缓存放到session 缓存/二级缓存中
		Employee employee1 =  (Employee)session.load(Employee.class,5);
		System.out.println(employee1);
		
//下面这个不会执行db 在二级缓存中查找到的，下面这个缓存之后  如果长时间使用只会，会放到一级缓存中去
		Employee employee2 =  (Employee)session.load(Employee.class,5);
		System.out.println(employee2);
		
		
		Employee employee3 =  (Employee)session.get(Employee.class,5);
		System.out.println(employee3);		
		
	}	
	
	
	private static void testCurrentSession(){
		Session session = MySessionFactory.getSessionFactory().getCurrentSession();
		Session session2 = MySessionFactory.getSessionFactory().getCurrentSession();
		
		System.out.println(session.hashCode()+"  下一个session是 "+session2.hashCode());
	}
	//使用模板
	private static void deleteEmployee() {		
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=null;		
			try {
				ts=session.beginTransaction();
				Employee employee =  (Employee)session.load(Employee.class, 3);
				session.delete(employee); 				
					ts.commit();
			} catch (Exception e) {
				// TODO: handle exception
				if(ts!=null){
					ts.rollback();
				}
	//抛出运行异常
				throw new RuntimeException(e.getMessage());
				
			}finally{
				if(session!=null&&session.isOpen()){
					session.close();
				}
			}
		
	}
	
	
	private static void deleteEmployee2() {
		//然后我们开始删除的操作了
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=session.beginTransaction();

		Employee employee =  (Employee)session.load(Employee.class, 1);
		session.delete(employee); 
		ts.commit(); 
		session.close();
				
	}
//使用模板
	private static void updateEmployee() {
		//现在开始修改数据库中的表的数据,和下面的东西是一样的但是这个需要占用很多的内存
		
		//所以我们将这个SessionFactory制作成单态的,通过制作一个工具类
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=null;			
			try {
				ts=session.beginTransaction();
				//修改用户Employee.java先获取用户，然后修改
				//load是可以通过主键来获取表的记录
				Employee employee =(Employee)session.load(Employee.class, 4);
				//下面这个就是相当于更新了	
				employee.setEmail("13151515@qq.com");
					ts.commit();
			} catch (Exception e) {
				// TODO: handle exception
				if(ts!=null){
					ts.rollback();
				}
	//抛出运行异常
				throw new RuntimeException(e.getMessage());
				
			}finally{
				if(session!=null&&session.isOpen()){
					session.close();
				}
			}
	}
	private static void updateEmployee2() {
		//现在开始修改数据库中的表的数据,和下面的东西是一样的但是这个需要占用很多的内存
		
		//所以我们将这个SessionFactory制作成单态的,通过制作一个工具类
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=session.beginTransaction();				
				//修改用户Employee.java先获取用户，然后修改
				//load是可以通过主键来获取表的记录
				Employee employee =(Employee)session.load(Employee.class, 5);
				//下面这个就是相当于更新了	
				employee.setEmail("发士大夫5@qq.com");
					ts.commit();
			
					session.close();
			
			
	}
	private static void addEmployee() {
		
		Session session = MySessionFactory.getSessionFactory().openSession();
		Transaction ts=null;			
			try {
				ts=session.beginTransaction();
				//加入一个对象(记录)Employee.java
				Employee u=new Employee();
				u.setEmail("1318830916@qq.com");
				u.setHiredate(new Date());
				u.setName("zhangyujin");
				//insert 
				//保存				
		//这个就是持久化对象了
				session.save(u);//添加该对象到数据库  //等于==inset into 。。被hibernate封装，将来不管用什么数据库都是可以试实现的
				ts.commit();
			} catch (Exception e) {
				// TODO: handle exception
				if(ts!=null){
					ts.rollback();
				}
	//抛出运行异常
				throw new RuntimeException(e.getMessage());
				
			}finally{
				if(session!=null&&session.isOpen()){
					session.close();
				}
			}
		
		
		
		
	}
	private static void addEmployee2() {		
			
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
		//保存，Employee.hbm.xml
		
//这个就是持久化对象了
		session.save(u);//添加该对象到数据库  //等于==inset into 。。被hibernate封装，将来不管用什么数据库都是可以试实现的
		ts.commit();//提交事务
		session.close();
	}	

	
	
	
//自己设立id
	private static void addEmployee3() {		
		
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
		u.setId(100); 
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
//将hibernate.cfg.xml换一个位置 
private static void addEmployee4() {		
		
		//对数据持久层操作 
		//1. 创建Configurateion该类主要是用于读取hibernate.cfg.xml
		Configuration cf=new Configuration().configure("com/hlj/config/hibernate.cfg.xml");
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

