package com.hlj.service;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;



import com.hlj.domain.Users;
import com.hlj.util.HibernateUtil;

public class UserService {
	public boolean checkUser(Users users){
		boolean a = false;

		String sql = "from Users where name='"+users.getName()+"'and password = '"+users.getPassword()+"'";
		List list = HibernateUtil.executeQuery(sql);
		if (list!=null) {
			
			a= true;
		}

			return a; 


	}
}
