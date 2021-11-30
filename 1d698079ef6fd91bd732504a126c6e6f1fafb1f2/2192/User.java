package com.hlj.domain;

public class User {

     String username;   
     String password;  
     int age;
    public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getUsername() {   
        return username;   
    }   
    public void setUsername(String username) {   
        this.username = username;   
    }    
    public String getPassword() {   
        return password;   
    }    
    public User(String username, String password, int age) {
		super();
		this.username = username;
		this.password = password;
		this.age = age;
	}
	public void setPassword(String password) {   
        this.password = password;   
    }   
}

