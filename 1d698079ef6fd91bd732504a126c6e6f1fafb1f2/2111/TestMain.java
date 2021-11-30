package com.hlj.view;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.hlj.domain.Department;
import com.hlj.domain.StudentMany;
import com.hlj.util.HibernateUtil;


public class TestMain implements updateEmployee {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		CreateTable();
//		AddStudentToDept();
//		fromfa();
//		getStudent();
//		getDepartmentStudent();
//		JL();
		testLazy();
	}
//将我们的表通过映射文件来生成	
//调用一个session 来加载hibernate 
//从而自动生成
	public static void CreateTable(){
		HibernateUtil.openSession();  
		
	}

	private static void testLazy(){
		
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
			
			 Department department = (Department) session.get(Department.class, 2);
		//测试 懒加载
		//	 session.close();
			 
			 Set<StudentMany> set = department.getStudentManys();
			 for(StudentMany s:set ){
				  
				 System.out.println(s.getName());
			 }
			 
			 System.out.println(department.getName());
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
	private static void JL(){
		
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
			
			 Department department = (Department) session.get(Department.class, 4);
			session.delete(department); 
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
	private static void AddStudentToDept(){
		
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
			StudentMany studentMany = new StudentMany();
			studentMany.setName("zhangyujin");
			Department dept = new Department();
			dept.setName("jialidun");
	//下面这个在测试级联的时候进行 注释了
		//	studentMany.setDept(dept);
			
		//	session.save(dept);
			
			Set<StudentMany> students=new HashSet<StudentMany>();
			students.add(studentMany);
			 
			dept.setStudentManys(students); 			
			session.save(dept);   
			
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
				 
				//第二种 将在自己department的xml文件中进行修改 将lazy 改为  false
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
		public static void getDepartmentStudent() {
			// TODO Auto-generated method stub
			//ͨ���ȡһ��sesion,��hibernate�������(config->����hibernate.cfg.xml)
			Session s=null;
			Transaction tx=null;
			StudentMany student1=null;
			try {
				//����ʹ�û�ģ��������.
				s=HibernateUtil.getCurrentSession();
				tx=s.beginTransaction();
				//第一种
				//String hql="from Student where dept.id=1";
			
				 
				Department department1=(Department) s.get(Department.class, 3);
		
				Set<StudentMany> stus= department1.getStudentManys();
				for(StudentMany ss: stus){
					System.out.println(ss.getName());
				}  
				
				

				Department department=new Department();
				department.setName("ffadf");
				
				StudentMany stu1=new StudentMany();
				stu1.setName("fadsfdasfads");
				StudentMany stu2=new StudentMany();
				stu2.setName("fadf");
				
				Set sets=new HashSet<StudentMany>();
				sets.add(stu1);
				sets.add(stu2);
				department.setStudentManys(sets);
				 
				s.save(department);
				
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
		
		public void getStuden(){
			
			StudentMany studentMany = new StudentMany();
			
		}
}

