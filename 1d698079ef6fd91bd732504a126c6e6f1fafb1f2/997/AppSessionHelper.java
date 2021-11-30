package com.duodian.admore.home.utils;

import com.duodian.admore.constants.AppConstants;
import com.duodian.admore.core.spring.RequestHolder;
import com.duodian.admore.entity.db.user.User;

import javax.servlet.http.HttpSession;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/4/13
 * version：1.0.0
 */
public class AppSessionHelper {

   
    public static final String SESSION_USER = "user";


    public static void initSession(User user){
        HttpSession session = RequestHolder.getSession();
        session.setAttribute(SESSION_USER,user);
    }

    public static void clearSession(){
        HttpSession session = RequestHolder.getSession();
        session.invalidate();
    }

    public static User getSessionUser(){
        HttpSession session = RequestHolder.getSession();
        User user = (User)session.getAttribute(SESSION_USER);
        return user;
    }


}
