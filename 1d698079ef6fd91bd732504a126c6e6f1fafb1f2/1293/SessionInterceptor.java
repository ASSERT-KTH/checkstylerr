package com.hlj.config.interceotor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/13
 * version：1.0.0
 */
public class SessionInterceptor implements HandlerInterceptor {

    private final String[] NO_FILTER_PAGES = {"/","/login"};

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        for (String c : NO_FILTER_PAGES){
            if (c.equals(uri)){
                return true;
            }
        }
        HttpSession session = httpServletRequest.getSession();
        if (session.getAttribute("healerjean") == null){
            invalidate(httpServletRequest,httpServletResponse);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }


    private void invalidate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        String ajax = request.getHeader("X-Requested-With");
        if (StringUtils.equals(ajax,"XMLHttpRequest")) {
            //设置登陆超时header
            response.addHeader("sessionout","true");
        } else {
            response.sendRedirect("/");
        }
    }

}
