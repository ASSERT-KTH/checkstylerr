package com.hlj.view;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.hlj.domain.Department;
import com.hlj.domain.StudentMany;
import com.hsp.util.HibernateUtil;


public class TestMain implements updateEmployee {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		CreateTable();
//		AddStudentToDept();
//		fromfa();
		getStudent();
	}
//将我们的表通过映射文件来生成	
//调用一个session 来加载hibernate 
//从而自动生成
	public static void CreateTable(){
		HibernateUtil.openSession();  
		
	}
	
	
	
	//添加一个学生，让这个学生添加到部门
	private static void AddStudentToDept(){
		
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
			StudentMany studentMany = new StudentMany();
			studentMany.setName("zhao");
			Department dept = new Department();
			dept.setName("english");
			studentMany.setDept(dept);
			
			session.save(dept);
			session.save(studentMany);
			
			ts.commit();  
			 
		} catch (Exception e) {
			
			if(ts!=null){
				ts.rollback();
			}
			throw new RuntimeException(e.getMessage()+"这个错误了");
		}finally{
			//关闭session
			if(session!=null&&session.isOpen()){
				session.close();
			}
			
		}		
	}
	
	
	//添加一个学生，让这个学生添加到部门
		private static void fromfa(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
				
				 List<Department> list = session.createQuery("from Department").list();
				  
				 for(Department obj: list){
					 
					 System.out.println(obj.getName());
				 }
				
				ts.commit();  
				 
			} catch (Exception e) {
				
				if(ts!=null){
					ts.rollback();
				}
				throw new RuntimeException(e.getMessage()+"这个错误了");
			}finally{
				//关闭session
				if(session!=null&&session.isOpen()){
					session.close();
				}
				
			}		
		}
		
		public static void getStudent() {
			// TODO Auto-generated method stub
			//ͨ���ȡһ��sesion,��hibernate�������(config->����hibernate.cfg.xml)
			Session s=null;
			Transaction tx=null;
			StudentMany student1=null;
			try {
				//����ʹ�û�ģ��������.
				s=HibernateUtil.getCurrentSession();
				tx=s.beginTransaction();
				//��ѯ3��ѧ��
				student1=(StudentMany) s.load(StudentMany.class, 2);
//解决懒加载的方法
				//第一种 显示初始化代理对象 ，通过这个得到 department
				//Hibernate.initialize(student1.getDept());
				
				//第二种 将在自己的xml文件中进行修改 将lazy 改为  false
				System.out.println(student1.getName()+" 所在的系"
					+student1.getDept().getName());
				tx.commit(); 
				  
			} catch (Exception e) {
				if(tx!=null){
					tx.rollback();
				}
			}finally{
				
				if(s!=null && s.isOpen()){
					s.close();
				}
			}		
		}

}

