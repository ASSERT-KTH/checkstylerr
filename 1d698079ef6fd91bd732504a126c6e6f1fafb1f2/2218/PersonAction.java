package cn.itcast.action;

import java.util.regex.Pattern;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

/*public class PersonAction extends ActionSupport{
	private String username;
	private String mobile;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String update(){
		ActionContext.getContext().put("message", "更新成功");
		return "message";
	}
	
	public String save(){
		ActionContext.getContext().put("message", "保存成功");
		return "message";
	}*/
	
/**
 * 对所有的方法进行校验
 */
	/*	@Override 
	public void validate() {//会对action中的所有方法校验
		if(this.username==null || "".equals(this.username.trim())){
			System.out.println(this.username.trim()+"trim处理**********************");
			this.addFieldError("username", "用户名不能为空");
		}
		if(this.mobile==null || "".equals(this.mobile.trim())){
			this.addFieldError("mobile", "手机号不能为空");
		}else{
			//正则表达式^1
			if(!Pattern.compile("^1[358]\\d{9}$").matcher(this.mobile).matches()){
				this.addFieldError("mobile", "手机号格式不正确");
			}
		} 
	}*/
/**
 * 对指定的方法进行校验
 * 	trim 要给它的属行注入值
 */
	//会对update()方法校验
	/*public void validateUpdate() {
		if(this.username==null || "".equals(this.username.trim())){
			this.addFieldError("username", "用户名不能为空");
		}
		if(this.mobile==null || "".equals(this.mobile.trim())){
			this.addFieldError("mobile", "手机号不能为空");
		}else{
			if(!Pattern.compile("^1[358]\\d{9}$").matcher(this.mobile).matches()){
				this.addFieldError("mobile", "手机号格式不正确");
			}
		}
	}
}*/



/**
 * 基于xml的校验
 * @author 荒凉
 *
 */
public class PersonAction extends ActionSupport{
	private String username;
	private String mobile;

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String update(){
		ActionContext.getContext().put("message", "更新成功");
		return "message";
	}
	
	public String save(){
		ActionContext.getContext().put("message", "保存成功");
		return "message";
	}	
}
	
	
	
	
	
	
	
	
	
