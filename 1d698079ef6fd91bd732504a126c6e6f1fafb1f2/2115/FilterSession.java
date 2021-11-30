package com.hlj.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hlj.util.HibernateUtil;



public class FilterSession extends HttpServlet implements Filter {

	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		// TODO Auto-generated method stub
		Session s=null;
		Transaction tx=null;
		try {  
			s =HibernateUtil.getCurrentSession();
			tx=s.beginTransaction();
//			假设有了这个doFilter 有了事物 会先在前面跑， 
//			当我们看到ok的的时候整个请求都会往回返， 
//			在一传工作全部完成之后，这个session关闭的情况不能像从前。看下面的finally
			arg2.doFilter(arg0, arg1);
			//System.out.prrintln("ok")
			//System.out.println("ok");x.
			tx.commit();
			 
		} catch (Exception e) {
			if(tx!=null){
				tx.rollback();
			}
			throw new RuntimeException(e.getMessage());
			// TODO: handle exception
		}finally{
			
			HibernateUtil.closeCurrentSession();
		}
		
		
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
