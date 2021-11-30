package com.cas.healerjean.client.controller;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);
    @GetMapping("hello")
    @ResponseBody
    public String home(HttpServletRequest request){



        String remoteUser =request.getRemoteUser();
        logger.info("1、request.getRemoteUser()"+remoteUser);



        //断言
        Assertion assertion = (Assertion) request.getSession().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        AttributePrincipal principal = assertion.getPrincipal();
        String username =  principal.getName();
        logger.info("2、AttributePrincipal.getName"+username);


         String user3 = AssertionHolder.getAssertion().getPrincipal().getName();
        logger.info("3、AssertionHolder.getAssertion().getPrincipal().getName()"+user3);

        /**
         * 加上 其他参数以后的
         */
        String email =   AssertionHolder.getAssertion().getPrincipal().getAttributes().get("email").toString();
        String address =   AssertionHolder.getAssertion().getPrincipal().getAttributes().get("address").toString();
        logger.info("4、邮箱和地址:"+email+address);
        return  null;
    }

    @GetMapping("/start")
    public String logout(){
        return "index";
    }
}
