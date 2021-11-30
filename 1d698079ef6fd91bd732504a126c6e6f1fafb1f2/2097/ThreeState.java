package com.hlj.view;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hlj.domain.Course;
import com.hlj.util.HibernateUtil;

public class ThreeState {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		three();
	}
//对象的三种状态
	private static void three(){
		
		Session session = null; 
		Transaction ts = null;

		try {  
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();

				Course course = new Course(); //course 就是瞬时态
				course.setCcredit(12);
				course.setCname("chinese");
				
					session.save(course); //这个时候course 既是 session的管理下，同时又被保存到数据库中去了
											//所以这个时候就是持久太
					course.setCname("chimecial");  //这个时候就会更新数据库的内容了，
					course.setCcredit(754);       //会同上面的同时更新
					
					session.delete(course); //如果有了这个语句，我们就不会更新了
					
			ts.commit(); 
			//关闭session
			if(session!=null&&session.isOpen()){
				session.close();
			}
		
		//这个时候没有人管理 了course ，所以这个就是托管状态
		//也就是游离状态，这个时候course 不会被hibernate检测到，也不会返回到数据库中
			//这里即使发生了变化也不会被检测到，但是会
			course.setCname("fa"); 
			System.out.println(course.getCname());
		} catch (Exception e) {
			
			if(ts!=null){
				ts.rollback();
			}
			throw new RuntimeException(e.getMessage()+"这个错误了");
		}finally{
			
			
		}		
	}
}
