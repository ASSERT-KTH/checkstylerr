package com.hlj.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 21/02/2017
 * version：1.0.0
 */
@Controller
@RequestMapping("")
public class LoginController {

    @RequestMapping("")
    public String page(){
        return "login.ftl";
    }

    @RequestMapping("login")
    public String login(String username, String password, HttpServletRequest request){
        if (StringUtils.equals(username,"healerjean") && StringUtils.equals(password,"123456")){
            request.getSession().setAttribute("healerjean","admore");
            return "redirect:/main";
        }
        return "login.ftl";
    }


    @RequestMapping("main")
    public String main(){
        return "main.ftl";
    }

}
