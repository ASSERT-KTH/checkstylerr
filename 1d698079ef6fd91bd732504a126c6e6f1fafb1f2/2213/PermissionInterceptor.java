package cn.itcast.interceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class PermissionInterceptor implements Interceptor {

	public void destroy() {
	}

	public void init() {
	}

	public String intercept(ActionInvocation invocation) throws Exception {
		Object user = ActionContext.getContext().getSession().get("user");
		if(user!=null) return 
				//用户名为空的时候 就是环绕执行
				invocation.invoke(); 
		
		//如果user不为null,代表用户已经登录,允许执行action中的方法
		ActionContext.getContext().put("message", "你没有权限执行该操作");
		return "success";
	}

}
