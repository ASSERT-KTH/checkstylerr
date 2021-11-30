package com.hlj.view;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;




import com.hlj.domain.Course;
import com.hlj.domain.Stucourse;
import com.hlj.domain.Student;
import com.hlj.util.HibernateUtil;


public class TestMain implements updateEmployee {

	public static void main(String[] args) {
//		selectAll();
//		selectOne();
		
		//StudentgetWhatCourse();//学生选择了几门课程
//		StudentgetCourse();
//		uniqueResule();
//		selectSexAndAge();
//		between();	
//		in();
//		averageGroupBy();
//		count();
//		countAndAdept();
//		oneObject();
//		sum();
//		Zhuwaijian();
//		StudentAndCourseAndStuCourse();
//		zhuwaijianGoupe();
//		showResultByPage(2);
//		canshu0();
//		canshu1();
//		FenYe();
//		addObject();
//		 updateObject();
//		selectFromStuCourse();
		HQlgetStuCourse();
	}	
	
	//检索所有学生的信息 
	private static void selectAll(){
		Session session = null; 
		Transaction ts = null;
		try {  
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
		//检索所有学生的信息 
	Query query=session.createQuery("from  Student"); 

	List<Student> list=query.list();
			for(Student e: list){  
				System.out.println("姓名"+e.getSname()+"性别"+e.getSsex());
			} 
//使用iterator迭代器进行输出
		Iterator<Student> iterator = list.iterator();
				while (iterator.hasNext()) { 
					Student student = iterator.next();
			System.out.println("iterator姓名"+student.getSname()+"性别"+student.getSsex());
 
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
	
	//检索学生的名字和有限字段
	//原则上我们是 可以像jdbc中全部查找出来的
	//hibernate 建议中都是必须都查出来
	//但是我们也要学会  查找有限的
	private static void selectOne(){
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
		//检索所有学生的信息 
	Query query=session.createQuery("select sname,adept from Student"); 
//这样我们就不可以使用下面的东西了，就会报错因为哦我们不能得到一个list
	List list=query.list();
	for(int i = 0 ; i < list.size();i++){
		Object []object = (Object[]) list.get(i); 
		System.out.println(object[0].toString()+""+object[1]);

	}
	
	Iterator iterator = list.iterator();
	while (iterator.hasNext()) {
		Object[] objects = (Object[]) iterator.next();
		System.out.println(objects[0].toString()+"fadsf"+objects[1]+"fasdf");
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
	
	
	//通过学生找到几门课程
	private static void StudentgetWhatCourse(){
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
		//检索所有学生的信息 
	Query query=session.createQuery("from Student"); 
//这样我们就不可以使用下面的东西了，就会报错因为哦我们不能得到一个list
	List<Student>  list=query.list();
	for(Student s: list){ 
		if(s.getStucourses().size()==0){
			System.out.println(s.getSname()+"没有选课");
		}else{
		System.out.println(s.getSname()+"选择了"+s.getStucourses().size()+"门课程");
			
		}
	
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

	
	//通过学生找到他选择的几门课程
	private static void StudentgetCourse(){
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
		//检索所有学生的信息 
	Query query=session.createQuery("from Student"); 
//这样我们就不可以使用下面的东西了，就会报错因为哦我们不能得到一个list
	List<Student>  list=query.list();
	for(Student s: list){ 
		if(s.getStucourses().size()==0){
			System.out.println(s.getSname()+"没有选课");
		}else{
		//System.out.println(s.getSname()+"选择了"+"课程表:  "+s.getStucourses()+"并且选择了"+s.getStucourses().size()+"门课程");
		
		Set<Stucourse> stucourses = s.getStucourses();
		for(Stucourse stc : stucourses){
				System.out.println(s.getSname()+"选择了"+stc.getCourse().getCname());
				
			}
		}
	
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
	
	
//确认只有一条记录的时候使用 uniqueResule
	private static void uniqueResule(){
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
		//检索所有学生的信息 
	Student student = (Student)session.createQuery("from Student where sid = '1'").uniqueResult(); 

		System.out.println(student.getSname());
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
	
	
	
	
//查看所有学生的性别和年龄，但是不能出现重复	
	private static void selectSexAndAge(){
		Session session = null; 
		Transaction ts = null;
		try { 
			session = HibernateUtil.getCurrentSession();
			 ts = session.beginTransaction();
		//检索所有学生的信息 
	Query query=session.createQuery("select distinct ssex,sage from Student"); 
//这样我们就不可以使用下面的东西了，就会报错因为哦我们不能得到一个list
	List list=query.list();
	for(int i = 0 ; i < list.size();i++){
		Object []object = (Object[]) list.get(i); 
		System.out.println(object[0].toString()+""+object[1]);

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
	
	
	

		private static void between(){
		
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
			//检索所有学生的信息 
		Query query=session.createQuery("from Student where sage between 20 and 23"); 

		List<Student> list=query.list();
				for(Student e: list){  
					System.out.println("姓名"+e.getSname()+"性别"+e.getSsex());
				} 
	//使用iterator迭代器进行输出
			Iterator<Student> iterator = list.iterator();
					while (iterator.hasNext()) { 
						Student student = iterator.next();
				System.out.println("iterator姓名"+student.getSname()+"性别"+student.getSsex());
	 
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
		private static void in(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
			//检索所有学生的信息 

			List<Student> list=session.createQuery("from Student where adept  in ('zijitian','computer')").list();
					//ȡ��1. for ��ǿ
					for(Student s:list){
				System.out.println(s.getSname()+" "+s.getSaddress()+" "+s.getAdept());
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
	//	，系里面的平均年龄object 对象 显示字段
		private static void averageGroupBy(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
		
//下面这个是两个字段了 ，系里面的平均年龄
		List<Object[]> list=session.createQuery("select avg(sage),adept from  Student group by adept").list();
					
					for(Object[] obj:list){
						System.out.println(obj[0].toString()+" "+obj[1].toString());
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
		
		//查询各个系有 人数 大于3 的系的名称
		//查询有系里有多少个学生		
		private static void count(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
		//查询各个系有 人数 大于3 的系的名称
		//查询有系里有多少个学生
				 //count 用于别名 as c1
				 List<Object[]> list=session.createQuery("select count(*) as c1,adept from  Student group by adept having count(*)>3").list();
					//ȡ��1. for ��ǿ
					for(Object[] obj:list){
						System.out.println(obj[0].toString()+" "+obj[1].toString());
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
		
		 //选择系里面男生有多少个			 
	
		private static void countAndAdept(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();

				 //选择系里面男生有多少个			 
				 List<Object[]> list=session.
							createQuery("select count(*) as c1,adept from  Student where ssex='m' group by adept").list();
							//ȡ��1. for ��ǿ
							for(Object[] obj:list){
								System.out.println(obj[0].toString()+" "+obj[1].toString());
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

//查询一个字段
		private static void oneObject(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();

					List<Object[]> list=session.
							createQuery("select sage from  Student where adept='computer'").list();
							//ȡ��1. for ��ǿ
							for(Object obj:list){
								System.out.println(obj.toString());
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
		private static void sum(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();

					List<Object[]> list=session.
							createQuery("select sum(grade) from Stucourse").list();
							//ȡ��1. for ��ǿ
							for(Object obj:list){
								System.out.println(obj.toString());
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
	//查询选修课为 111 的最高分和最低分
		private static void Zhuwaijian(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();

					List<Object[]> list=session.
							createQuery("select 111,max(grade),min(grade) from Stucourse where course.cid=111").list();
							//ȡ��1. for ��ǿ
							for(Object[] obj:list){
								System.out.println(obj[0].toString()+" max="+obj[1].toString()+" min="+obj[2].toString());
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
	//显示各科考试不合格的成绩和学生
		private static void StudentAndCourseAndStuCourse(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();

					List<Object[]> list=session.
							createQuery("select student.sname,course.cname,grade from Stucourse where grade>=60").list();
							//ȡ��1. for ��ǿ
							for(Object[] obj:list){
								System.out.println(obj[0].toString()+" "+obj[1].toString()+" "+obj[2].toString());
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
		//选择成绩 大于60的 学生的数目和所在的系
		private static void zhuwaijianGoupe(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();

					List<Object[]> list=session.
							createQuery("select count(*),student.adept from Stucourse where grade>60 group by student.adept").list();
							//ȡ��1. for ��ǿ
							for(Object[] obj:list){
								System.out.println(obj[0].toString()+" "+obj[1].toString());
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
		//��ҳ����
		private static void showResultByPage(int pageSize){
			//���÷�ҳ�ı���
			int pageNow=1;
			int pageCount=1;//����
			int rowCount=1;//�����Ҫ��ѯ
			
			Session session=null;
			Transaction tx=null;
			try {
				session=HibernateUtil.getCurrentSession();
				tx=session.beginTransaction();
				
				//��ѯ��rowcount
				rowCount=Integer.parseInt(session.createQuery("select count(*) from Student").uniqueResult().toString());
				pageCount=(rowCount-1)/pageSize+1;
				
				//�������ǿ���ѭ������ʾÿҳ����Ϣ
				for(int i=1;i<=pageCount;i++){
					System.out.println("************第"+i+"页************");
			//设置第几条结果开始 然后 乘以  每页的大小 得到的集合
					List<Student> list=session.createQuery("from Student").setFirstResult((i-1)*pageSize)
					.setMaxResults(pageSize).list();
					for(Student s: list){
						System.out.println(s.getSname()+" "+s.getAdept());
					}
					
				}
				
				tx.commit();
				
			}catch(Exception e){
				e.printStackTrace();
				if(tx!=null){
					tx.rollback();
				}
				throw new RuntimeException(e.getMessage());
			}finally{
				if(session!=null&&session.isOpen()){
					session.close();
				}
			}
			
		}
//使用方法进行查找
		private static void canshu(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
  
				 String hql="select sname,saddress from Student where adept=? and sage>?";
					String parameters[]={"computer","3"};  
					List<Object[]> list= HibernateUtil.executeQuery(hql,parameters);
					for(Object[] s: list){ 
						System.out.println(s[0].toString()+" "+s[1].toString());
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
//使用 ？ 进行
		private static void canshu0(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
  			Query query = session.createQuery("select sname,saddress from Student where adept=? and sage>?");
  			
  			query.setString(0, "computer").setString(1, "20");
  			List<Object[]> list = query.list();
  			
					/*List<Object[]> list=session.
							createQuery("select sname,saddress from Student where adept=? and sage>?").setString(0, "computer").setString(1, "20").list();
					 */
							for(Object[] obj:list){
								System.out.println(obj[0].toString()+" "+obj[1].toString());
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
//使用冒号进行
		private static void canshu1(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
  			
					List<Object[]> list=session.
							createQuery("select sname,saddress from Student where adept=:ad and sage>:age").setString("ad", "computer").setString("age", "20").list();
					 
							for(Object[] obj:list){
								System.out.println(obj[0].toString()+" "+obj[1].toString());
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
		
		
	//使用函数进行分页
		//使用冒号进行
				private static void FenYe(){
					
					Session session = null; 
					Transaction ts = null;
					try { 
						session = HibernateUtil.getCurrentSession();
						 ts = session.beginTransaction();
		  			
						 String hql="select sname,saddress from Student ";
							String parameters[]=null; 
							List<Object[]> list= HibernateUtil.
							executeQueryByPage(hql, parameters, 2, 3) ;
							for(Object[] s: list){ 
								System.out.println(s[0].toString()+" "+s[1].toString());
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
				
				private static void addObject(){
					
					Session session = null; 
					Transaction ts = null;
					try { 
						session = HibernateUtil.getCurrentSession();
						 ts = session.beginTransaction();
		  			
						 Course c=new Course();
							c.setCname("servlet");
							c.setCid(2);
							c.setCcredit(5); 
							HibernateUtil.save(c);
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
				private static void updateObject(){
					
					Session session = null; 
					Transaction ts = null;
					try { 
						session = HibernateUtil.getCurrentSession();
						 ts = session.beginTransaction();
		  			
							String hql="update Student set sage=sage+1 where sname=?";
							String parameters[]={"zhang"};
							try { 
								HibernateUtil.executeUpdate(hql, parameters);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								// TODO: handle exception
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
		private static void selectFromStuCourse(){
					
					Session session = null; 
					Transaction ts = null;
					try { 
						session = HibernateUtil.getCurrentSession();
						 ts = session.beginTransaction();
		  			
						 String hql="select student.sname,student.adept from Stucourse where course.cid=?";
							String parameters[]={"111"}; 
							List<Object[]> list=HibernateUtil.executeQuery(hql, parameters);
							for(Object[] s:list){
								System.out.println(s[0].toString()+" "+s[1].toString());
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
		private static void HQlgetStuCourse(){
			
			Session session = null; 
			Transaction ts = null;
			try { 
				session = HibernateUtil.getCurrentSession();
				 ts = session.beginTransaction();
  			 
				 String hql="from Stucourse where course.cid=111";
				 List<Stucourse> list=HibernateUtil.executeQuery(hql, null);
				 //������������һ���½����.
				 for(Stucourse sc:list){ 
		//下面这个我们的session中 在执行了玩一个之后session已经关闭了，这个就是懒加载，这里我们将懒加载关闭
				 	System.out.println(sc.getGrade()+sc.getStudent().getSname());
				 	
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
	
}

