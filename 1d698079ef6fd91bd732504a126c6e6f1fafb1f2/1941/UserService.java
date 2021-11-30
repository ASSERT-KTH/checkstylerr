package com.hlj.service;

public class UserService {

	private String name;
	 
	private BybService bybService;

	public BybService getBybService() {
		return bybService;
	}

	public void setBybService(BybService bybService) {
		this.bybService = bybService;
	}
 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void sayHello(){
		System.out.println("hello "+name );
		bybService.sayBye();
	}
	
}
