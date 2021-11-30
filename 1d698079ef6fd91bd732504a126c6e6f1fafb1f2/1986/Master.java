package com.hsp.autowire;

public class Master {

	private String name;
	private Dog dog;
	
	
	
	
/*	public Master(Dog dog) {
//		测试construstor
		System.out.println("构造函数被调用");
		
		this.dog = dog;
	}*/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Dog getDog() {
		return dog;
	}
	public void setDog(Dog dog) {
		this.dog = dog;
	}
}
