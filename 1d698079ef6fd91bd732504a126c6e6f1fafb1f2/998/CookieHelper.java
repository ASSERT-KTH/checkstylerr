package com.duodian.youhui.admin.utils.cookie;

import com.duodian.youhui.admin.utils.RequestHolder;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Desc: Cookie 工具类
 * @Date:  2018/9/12 下午1:02.
 */

public class CookieHelper {




    public static void setCookie(String key,String value,Integer seconds){
        HttpServletResponse response = RequestHolder.getResponse();
        Cookie add = new Cookie(key,value);
        add.setMaxAge(seconds);
        add.setPath("/");
        response.addCookie(add);
    }

    public static String getCookieValue(String key){
        HttpServletRequest request = RequestHolder.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null){
            return null;
        }
        for (Cookie cookie : cookies){
            if (StringUtils.equals(cookie.getName(), key)){
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void clearCookie(String key){
        HttpServletResponse response = RequestHolder.getResponse();
        Cookie clear = new Cookie(key,"");
        clear.setMaxAge(0);
        clear.setComment("清除cookie");
        clear.setPath("/");
        response.addCookie(clear);
    }


}
