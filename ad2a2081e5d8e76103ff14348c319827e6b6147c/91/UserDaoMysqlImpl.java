package com.mashibing.spring.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.mashibing.spring.dao.UserDao;
import com.mashibing.spring.entity.User;


/**
 * һ���߳� ��������
 * ��һ���߳� �ر�����
 * 
 * ��Ӧ���� -> Connection ��
 * 
 * ThreadLocal 
 * @author Administrator
 *
 */

@Repository("daoMysql")
public class UserDaoMysqlImpl implements UserDao {

	@Autowired
	User user;
	
	public User getUserByName(String name) {
		System.out.println("�û������С�����");
		return user;
	}

}
