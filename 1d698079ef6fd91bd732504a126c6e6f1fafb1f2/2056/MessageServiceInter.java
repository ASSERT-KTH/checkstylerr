package com.hlj.service.inter;

import java.util.List;

import com.hlj.domain.Message;
import com.hlj.domain.Users;

public interface MessageServiceInter {
	//现实用户接收到的所有的message
	public List<Message> showMessages();
}
