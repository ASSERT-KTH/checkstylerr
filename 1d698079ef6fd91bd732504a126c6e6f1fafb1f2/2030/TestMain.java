package com.hlj.view;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;




import com.hlj.domain.IdCard;
import com.hlj.domain.Person;
import com.hlj.util.HibernateUtil;

public class TestMain {
 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//���һ��Person/idcard
		
		Session s=null;
		Transaction tx=null;
		
		try {
			//����ʹ�û�ģ��������.
			s=HibernateUtil.getCurrentSession();
			tx=s.beginTransaction();
		
			Person p1=new Person();
			p1.setId(123456);
			p1.setName("fasdf");
			IdCard idCard=new IdCard();
			idCard.setId(19456);
			idCard.setValidateDte(new Date());
			
			idCard.setPerson(p1);//将 person 放入idcard
			s.save(p1);//�ȱ�����
			s.save(idCard);
			
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
		
		
	
		
		
	}

	

}
