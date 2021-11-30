package com.hlj.zuihou;

import com.hlj.tongbu.synchronizedTongbu;

public class Info {
	String name;
	String content;
public synchronized void set(String name,String content){
	this.name = name;

	this.content = content;

}
public synchronized void get(){
	
	System.out.println(this.getContent()+this.getName()); 


}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
