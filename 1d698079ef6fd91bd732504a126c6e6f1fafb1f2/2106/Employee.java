package com.hlj.domain;

import java.io.Serializable;

//这个pojo，domain,javabean按照标准序列应该序列化，目的是可以唯一标识改对象，同时可以在网络和文件传输
//就是让他继承implements Serializable
public class Employee implements Serializable{

	/**
	 * 
	 */   
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name; 
	private String email;
	private java.util.Date hiredate;
	public Integer getId() {
		return id; 
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public java.util.Date getHiredate() {
		return hiredate;
	}
	public void setHiredate(java.util.Date hiredate) {
		this.hiredate = hiredate;
	}
}
