package com.hlj.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.hlj.form.UserForm;

public class LoginAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		UserForm userForm = (UserForm)form;
		System.out.println("用户名 +"+userForm.getUsername());
		
	
		if(userForm.getPassword().equals("123")){
	

			return mapping.findForward("ok");
		}else { 
			return mapping.findForward("err");

		}
		
			}

}
