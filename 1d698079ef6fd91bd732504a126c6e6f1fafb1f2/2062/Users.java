package com.hlj.domain;

import java.util.List;
import java.util.Set;

public class Users {

	private Integer userid;
	private String username;
	private String password;
	//这里用户 可以发动信息，也可以 接收信息 所以这个就是  one-to-many
	
	private Set<Message> sendMessages;
	private Set<Message> getMessages;
	
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
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
	public void setPassword(String password) {
		this.password = password;
	}
	public Set<Message> getSendMessages() {
		return sendMessages;
	}
	public void setSendMessages(Set<Message> sendMessages) {
		this.sendMessages = sendMessages;
	}
	public Set<Message> getGetMessages() {
		return getMessages;
	}
	public void setGetMessages(Set<Message> getMessages) {
		this.getMessages = getMessages;
	}
	
	
}
