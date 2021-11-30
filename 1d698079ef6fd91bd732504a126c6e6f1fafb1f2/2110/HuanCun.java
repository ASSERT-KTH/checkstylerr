package com.hlj.view;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;

import com.hlj.domain.StudentMany;
import com.hlj.util.HibernateUtil;
import com.hlj.util.MySessionFactory;

public class HuanCun {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Session s=null;
		Transaction tx=null;
		 
		try {
			//我们使用基础模板来讲解.
			s=HibernateUtil.openSession();
			tx=s.beginTransaction();
			 
			//查询45号学生
			
//放入一次
			StudentMany stu1=(StudentMany) s.get(StudentMany.class, 2);//45->一级缓存		
			System.out.println(stu1.getName());
			
			
			tx.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			if(tx!=null){
				tx.rollback();
			}
		}finally{
			
			if(s!=null && s.isOpen()){
				s.close();
			}
		}
		
		System.out.println("*********************************");
		try {
			//我们使用基础模板来讲解.
			s=HibernateUtil.openSession();
			tx=s.beginTransaction();
			
			//查询45号学生
		
			StudentMany stu1=(StudentMany) s.get(StudentMany.class, 2);	
			System.out.println(stu1.getName());
			
			StudentMany stu3=(StudentMany) s.get(StudentMany.class, 3);	
			System.out.println(stu3.getName());
				tx.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			if(tx!=null){
				tx.rollback();
			}
		}finally{
			
			if(s!=null && s.isOpen()){
				s.close();
			}
		}
		
		//完成一个统计，统计的信息在Sessfactory
		//SessionFactory对象. 
		Statistics statistics= HibernateUtil.getSessionFactory().getStatistics();
		System.out.println(statistics);  
		System.out.println("放入"+statistics.getSecondLevelCachePutCount());
		System.out.println("命中"+statistics.getSecondLevelCacheHitCount());
		System.out.println("错过"+statistics.getSecondLevelCacheMissCount());

	}

}
