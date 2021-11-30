package com.hlj.testUUID;

import java.util.UUID;

import org.omg.CORBA.StringHolder;

public class TestUUID {
	public static String getUUIdName(String filename){
		
		String uuid = UUID.randomUUID().toString();
		
	//	System.out.println(uuid);
		//现在考虑  将文件名filename.jpg 变为 	uuId.jpg
	//	String filename= "abcdefghijklmn.jpg";
		int begainName = filename.lastIndexOf(".");
	//取得文件的前面的名字
	//	String newName = filename.substring(0, filename.lastIndexOf("."));
	
		// 获取后缀名 因为是包头不包尾所以就直接length
	//	String newName = filename.substring(begainName, filename.length());
	
	//制作新名字
		String newName = uuid+filename.substring(begainName, filename.length());
		System.out.println(newName);
		return newName; 

	} 
}
