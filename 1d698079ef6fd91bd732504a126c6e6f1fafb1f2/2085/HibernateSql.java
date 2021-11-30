package com.hlj.util;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hlj.domain.Users;

public class HibernateSql {
	
	
	public List executeQuery(String sql){
		
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
	
	public boolean check(String sql){
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
}	
