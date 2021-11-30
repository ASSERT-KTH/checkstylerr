package com.hlj.view;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;



import com.hlj.util.HibernateUtil;


public class TestMain implements updateEmployee {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	CreateTable();

	}
//将我们的表通过映射文件来生成	
//调用一个session 来加载hibernate 
//从而自动生成
	public static void CreateTable(){
		HibernateUtil.openSession();  
		
	}
	
	
	
}

