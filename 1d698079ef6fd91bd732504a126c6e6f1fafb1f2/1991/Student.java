package com.hlj.ioc;


//java�඼��һ��Ĭ�ϵ��޲ι��캯��
public class Student {

	String name;

	public Student(){
		System.out.println("我说大连工业大学的张宇晋");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		//this.name = name;
	//当我们实力容器的时候，
		System.out.println("这个是名字ֵ"+name);
	}
	
	
}
