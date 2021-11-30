package com.sina.service.imp;

import java.util.List;

import com.hlj.domain.Message;
import com.hlj.domain.Users;
import com.hlj.service.inter.MessageServiceInter;
import com.hlj.util.HibernateUtil;

public class MessageServiceImp extends BaseServiceImp implements MessageServiceInter{

	@Override
	public List<Message> showMessages() {
		// TODO Auto-generated method stub 
		String hql="from Message";
		
		List<Message> messages = HibernateUtil.executeQuery(hql, null);		
		System.out.println(messages.size() ); 
		return messages; 
	} 
	
}
