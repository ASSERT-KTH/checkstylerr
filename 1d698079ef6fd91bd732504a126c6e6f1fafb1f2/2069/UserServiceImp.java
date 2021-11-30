package com.sina.service.imp;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

import com.hlj.domain.Users;
import com.hlj.service.inter.UsersServiceInter;
import com.hlj.util.HibernateUtil;

 
public class UserServiceImp extends BaseServiceImp implements UsersServiceInter{
	

	
	
	/**验证用户是不是合法
	 * @funtion 验证用户是不是合法
	 * @param users
	 * @return
	 */ 	
		
public boolean checkUser2(Users users){
		

		String sql = "from Users where username='"+users.getUsername()+"'and password = '"+MyTools.Md5.MD5Encrypt(users.getPassword())+"'";
		List list = HibernateUtil.executeQuery(sql);
		if (list.size()!=0) {
			 
			return true;
		}
			return false; 

	}
	
	
}
