package com.hlj.util;
import java.util.List;



import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
final public class HibernateUtil {
	private static SessionFactory sessionFactory=null;
	//ʹ���ֲ߳̾�ģʽ
	private static ThreadLocal<Session> threadLocal=new ThreadLocal<Session>();
	private HibernateUtil(){};
	static {
		sessionFactory=new Configuration().configure().buildSessionFactory();
	}
	
	//��ȡȫ�µ�ȫ�µ�sesession
	public static Session openSession(){
		return sessionFactory.openSession();
	}
	//��ȡ���̹߳�����session
	public static Session getCurrentSession(){
		
		Session session=threadLocal.get();
		//�ж��Ƿ�õ�
		if(session==null){
			session=sessionFactory.openSession();
			//��session�������õ� threadLocal,�൱�ڸ�session�Ѿ����̰߳�
			threadLocal.set(session);
		}
		return session;
		
		
	}
	
	//ͳһ��һ���޸ĺ�ɾ��(���� hql) hql"delete upate ...??"
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
	
	//ͳһ����ӵķ���
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
	
	
	//�ṩһ��ͳһ�Ĳ�ѯ����(���ҳ) hql ��ʽ from ��  where ����=? ..
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
	
	//�ṩһ��ͳһ�Ĳ�ѯ���� hql ��ʽ from ��  where ����=? ..
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
	
	
}
