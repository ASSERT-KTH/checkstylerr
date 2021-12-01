package com.mashibing.springboot.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.entity.Permission;

/**
 * 用户权限处理
 * @author Administrator
 *
 */
@Component
@WebFilter(urlPatterns = "/*")
public class AccountFilter implements Filter {

	
	// 不需要登录的 URI
	private final String[] IGNORE_URI = {
			"/index","/account/validataAccount",
			"/css/","/js/","/account/login","/images",
			"/errorPage"
		};
	
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		
		HttpServletRequest request =  (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		
		String uri = request.getRequestURI();
		
		// 当前访问的URI是不是 在Ignore列表里  
		System.out.println("uri:" + uri);
		boolean pass = canPassIgnore(uri);
		if (pass) {
			// 在的话 放行 
			chain.doFilter(request, response);
			return;
		}
		
		// 是否已登录，从session里找 Account
		
		Account account = (Account)request.getSession().getAttribute("account");
		System.out.println("getSession account:" + account);
		if (null == account) {
			// 没登录 跳转登录页面
			
			response.sendRedirect("/account/login");
			return;
		}
		
		
		// 已登录用户是否有权限访问当前页面
//		if(!hasAuth(account.getPermissionList(),uri)) {
//			
//			request.setAttribute("msg", "您无权访问当前页面:" + uri);
//			request.getRequestDispatcher("/errorPage").forward(request, response);
//			return;
//		}
		
		System.out.println("----filter----" + uri);
		chain.doFilter(request, response);
		
	}

//	private boolean hasAuth(List<Permission> permissionList, String uri) {
//		// TODO Auto-generated method stub
//		for (Permission permission : permissionList) {
//			if(uri.startsWith(permission.getUri())) {
//				return true;
//			}
//		}
//		return false;	
//	}

	private boolean canPassIgnore(String uri) {

		// /index = uri

		// 判断 访问的URI 起始部分 是否包含 Ignore 
		// 下级目录资源也能访问
		
		for (String val : IGNORE_URI) {
			
			if(uri.startsWith(val)) {
				return true;
			};
		}
		return false;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		// 加载 Filter 启动之前需要的资源
		System.out.println("---------------AccountFilter Init....");
		
		Filter.super.init(filterConfig);
	}
	
	

}
