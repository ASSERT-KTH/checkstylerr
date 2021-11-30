package com.hlj.userseivice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import org.apache.struts.apps.mailreader.dao.User;

import co.hlj.domain.Users;

import com.hlj.util.SqlHelper;
import com.hlj.util.SqlHelper1;

public class UserService {

	public boolean addUser(Users user){
		boolean flag = true;
		try {
			
		
		String sql  = "insert into users values(?,?,?)"; 
		String []parameter= {user.getUsername(),user.getPhotoBefore(),user.getPhotoNow()};
		
		SqlHelper1.executeUpdate(sql, parameter);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(); 
			return false;
		}			
		return true;
	}
	public ArrayList getUsers(){
		
		System.out.println("正在使用 getUsers "); 
		 SqlHelper sqlHelper = new SqlHelper();
		String sql = "select * from users";
		
		ArrayList arrayList =sqlHelper.executeQuery(sql,null);
		ArrayList<Users> users = new ArrayList<Users>();			
		if(arrayList.size()!=0){     
			for (int i = 0 ; i< arrayList.size() ; i++){
	//��������arrayList.get(0); �ǵõ����ǵĵ�һ����¼�� 
				Object objects[] = (Object[])arrayList.get(i);   
				Users user = new Users();  
		//object ��һ����Ŀ��������ͨ��ĵ�����Ķ�������Ȼ�����ת�����ַ�  
				user.setUsername(objects[0].toString());
				user.setPhotoBefore(objects[1].toString());
				user.setPhotoNow(objects[2].toString());				
				users.add(user);
			}				
		}				
		return users;		
	}
	
	public Users getUser(String username){
		String sql = "select * from users where username = ?";
		String parameter[] ={username};
		 
		ArrayList arrayList =SqlHelper1.executeQueryArrayList(sql, parameter);
		System.out.println("这个的大小为"+arrayList.size()); 
		Users user = new Users();	 		
		if(arrayList.size()!=0){     
			for (int i = 0 ; i< arrayList.size() ; i++){
	//��������arrayList.get(0); �ǵõ����ǵĵ�һ����¼�� 
				Object objects[] = (Object[])arrayList.get(i);   
				
		//object ��һ����Ŀ��������ͨ��ĵ�����Ķ�������Ȼ�����ת�����ַ�  
				user.setUsername(objects[0].toString());
				user.setPhotoBefore(objects[1].toString());
				user.setPhotoNow(objects[2].toString());				
			
			}				
		}	
		
		return user;
		
		
	}
}
