package com.hlj.filters;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import javax.servlet.*;

public class MyFilter extends HttpServlet implements Filter {
 


//主要就是在这里进行过滤
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
		//设置接收编码
		request.setCharacterEncoding("utf-8");
//下面这句话非常重要
		response.setContentType("text/html;charset=GBK");
		chain.doFilter(request, response); 
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
