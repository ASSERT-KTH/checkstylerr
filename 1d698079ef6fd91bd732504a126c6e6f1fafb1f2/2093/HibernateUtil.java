package com.hlj.util;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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
	
	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}
	
	
	/**
	 * 获取和线程关联的session
	 * @return
	 */
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
/**
 * 关闭CurrentSession	
 */
	public static void closeCurrentSession(){
		
		Session s=getCurrentSession();
		
		if(s!=null&& s.isOpen() ){
			s.close();
			threadLocal.set(null);
		}
	}
	
	public static List executeQuery(String sql){
		
		Session session = null; 
		Transaction ts = null;
		List list = null;
			try { 
				session = HibernateUtil.getCurrentSession(); 
				ts = session.beginTransaction(); 
				list=session.createQuery(sql).list();	
				
				ts.commit();
				
			} catch (Exception e) {
				
				if(ts!=null){
					ts.rollback();
				}
				throw new RuntimeException(e.getMessage()+"错误了");
			}finally{
				//关闭session
				if(session!=null&&session.isOpen()){
					session.close();
				}
				
			}
		
		
		return list;
		
	}
/**
 * 检查用户名 	
 * @param sql
 * @return
 */
	public static boolean check(String sql){
		boolean a = false;
		Session session = null;
		Transaction ts = null;
		List list = null;
		
			try { 
				session = HibernateUtil.getCurrentSession(); 
				ts = session.beginTransaction(); 
				list=session.createQuery(sql).list();	
				if (list!=null) {
					a = true;
				} 
				ts.commit();
				
			} catch (Exception e) {
				
				if(ts!=null){
					ts.rollback();
				}
				throw new RuntimeException(e.getMessage()+"错误了");
			}finally{
				//关闭session
				if(session!=null&&session.isOpen()){
					session.close();
				}
				
			}
		
		
		return a;
		
	}
/**
 * 进行更新	
 * @param hql
 * @param parameters
 */
public static void executeUpdate(String hql,String [] parameters){
		
		Session s=null;
		Transaction tx=null;
		
		try {
			s=openSession();
			tx=s.beginTransaction();
			Query query=s.createQuery(hql);
			//���ж��Ƿ��в���Ҫ��
			if(parameters!=null&& parameters.length>0){
				for(int i=0;i<parameters.length;i++){
					query.setString(i, parameters[i]);
				}
			}
			
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
			// TODO: handle exception
		}finally{
			
			if(s!=null&&s.isOpen()){
				s.close();
			}
			
		}
		
	}
//如果要配置opensissionInView
//同意的更新 和删除，到时候session中的提交在 过滤器中，就是说整个过程中我们的
public static void executeUpdateOpenInView(String hql,String [] parameters){
						
	Session s=getCurrentSession();
	
	
	Query query=s.createQuery(hql);
	//���ж��Ƿ��в���Ҫ��
	if(parameters!=null&& parameters.length>0){
		for(int i=0;i<parameters.length;i++){
			query.setString(i, parameters[i]);
		}
	}
	query.executeUpdate();
}
/**
 * 保存对象
 * @param obj
 */
	public  static void save(Object obj){
		Session s=null;
		Transaction tx=null;
		
		try {
			s=openSession();
			tx=s.beginTransaction();
			s.save(obj);
			tx.commit();
		} catch (Exception e) {
			if(tx!=null){
				tx.rollback();
			}
			throw new RuntimeException(e.getMessage());
			// TODO: handle exception
		}finally{
			if(s!=null && s.isOpen()){
				s.close();
			}
		}
		
	}
	
	
/**
 * 进行分页
 * @param hql
 * @param parameters
 * @param pageSize  每页几条记录
 * @param pageNow
 * @return
 */
	public static List executeQueryByPage(String hql,String [] parameters,int pageSize,int pageNow){
		Session s=null;
		List list=null;
		
		try {
			s=openSession();
			Query query=s.createQuery(hql);
			//���ж��Ƿ��в���Ҫ��
			if(parameters!=null&& parameters.length>0){
				for(int i=0;i<parameters.length;i++){
					query.setString(i, parameters[i]);
				}
			}
			
			query.setFirstResult((pageNow-1)*pageSize).setMaxResults(pageSize);
			
			list=query.list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
			// TODO: handle exception
		}finally{
			
			if(s!=null&&s.isOpen()){
				s.close();
			}
			
		}
		return list;
	}
	
	/**
	 * 
	 * 取得List集合
	 * @param hql
	 * @param parameters
	 * @return
	 */
	public static List executeQuery(String hql,String [] parameters){
		
		Session s=null;
		List list=null;
		 
		try {
			s=openSession();
			Query query=s.createQuery(hql);
			//���ж��Ƿ��в���Ҫ��
			if(parameters!=null&& parameters.length>0){
				for(int i=0;i<parameters.length;i++){
					query.setString(i, parameters[i]);
				}
			}
			list=query.list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
			// TODO: handle exception
		}finally{
			
			if(s!=null&&s.isOpen()){
				s.close();
			}
			
		}
		return list;
	}
	
/**
 * 通过id找 对象	
 * @param clazz
 * @param id
 * @return
 */
	public static Object findById(Class clazz,java.io.Serializable id){

		Session s=null;
		Transaction tx=null;
		Object obj=null;
		try {
			s=openSession();
			 
			tx=s.beginTransaction();
			obj=s.load(clazz, id);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
			// TODO: handle exception
		}finally{
			
			if(s!=null&&s.isOpen()){
				s.close();
			}
			
		}
		
		return obj;
	}

	
}
