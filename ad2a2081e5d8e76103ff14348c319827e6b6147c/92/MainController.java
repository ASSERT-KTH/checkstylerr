package com.mashibing.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.mashibing.spring.entity.User;
import com.mashibing.spring.service.MainService;

@Controller("mainController")
public class MainController {

	/**
	 * �����߼���ת
	 * ��web�����£���Controller���Ƚ���
	 * @return
	 */
	
	@Autowired
	private MainService srv;
	
	public String list() {
		
		String loginName = "zhangfg";
		String password = "123456";
		User user = srv.login(loginName,password);
		
		if(user == null) {
			return "��¼ʧ��";
		}else {
			return "��¼�ɹ�";
		}
	}
	
	
	// get/set ���������� -> �ϵ��ӽ�  Proxy CGLib
}
