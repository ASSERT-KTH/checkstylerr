package com.hlj.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.hlj.form.UserForm;



//这是一个action 表示小队长 

public class LoginAction extends Action {

	
	//重新编写一个方法这个方法会  execute 会被自动调用，相当于是 servlet/serviece/doPost doGet方法
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
//将form 转化成对应的UserForm	
		
		UserForm userForm = (UserForm)form;
		System.out.println("用户名 +"+userForm.getUsername());
		
		if(userForm.getUsername().equals("HealerJean")){
			
			return mapping.findForward("ok");
		}else { 
			return mapping.findForward("err");

		}
		
		// TODO Auto-generated method stub
	
		
	
	}

}
