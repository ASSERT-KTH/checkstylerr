package com.hlj.form;

import org.apache.struts.action.ActionForm;


	/**  
	 * 用户表单类,用于填充数据  
	 */  
	public class UserForm extends ActionForm {   
		
	    //在这里为了规范：定义属性名时应该和JSP页面的控件名称一样    
		
	    //实际只需保证set和get方法和属性名有关联 set属性名   
		
		
	    private String username;   
	    private String password;   
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
}
