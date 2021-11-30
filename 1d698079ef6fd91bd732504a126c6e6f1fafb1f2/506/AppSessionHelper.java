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

    public static void initSession(User user){
        HttpSession session = RequestHolder.getSession();
        session.setAttribute(AppConstants.SESSION_USER,user);
    }

    public static void clearSession(){
        HttpSession session = RequestHolder.getSession();
        session.invalidate();
    }

    public static User getSessionUser(){
        HttpSession session = RequestHolder.getSession();
        User user = (User)session.getAttribute(AppConstants.SESSION_USER);
        return user;
    }

    public static User getValidateSessionUser(){
        HttpSession session = RequestHolder.getSession();
        User user = (User)session.getAttribute(AppConstants.SESSION_VALIDATE_USER);
        return user;
    }

    public static void initValidateSession(User user){
        HttpSession session = RequestHolder.getSession();
        session.setAttribute(AppConstants.SESSION_VALIDATE_USER,user);
    }



    public static final String SESSION_USER_ID = "_session_user_id_";
    public static final String SESSION_USER = "_session_user_";
    public static final String SESSION_ACCOUNT = "_session_account_";
    public static final String SESSION_HEADIMG = "_session_headimg_";


    public static String getRemoteUserAccount(){
        return AssertionHolder.getAssertion().getPrincipal().getName();
    }

    public static Long getRemoteUserId(){
        return Long.parseLong(AssertionHolder.getAssertion().getPrincipal().getAttributes().get("id").toString());
    }

    public static String getRemoteUserName(){
        return AssertionHolder.getAssertion().getPrincipal().getAttributes().get("name").toString();
    }




    public static SysAdminUser getSessionUser(){
        HttpSession session = RequestHolder.getSession();
        return (SysAdminUser) session.getAttribute(AppConstants.SESSION_ADMIN_USER);
    }


    
}
