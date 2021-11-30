package com.hlj.sso.client.pac4j.controller;


import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("clientpac4j")
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
        String id =   AssertionHolder.getAssertion().getPrincipal().getAttributes().get("id").toString();

        String email =   AssertionHolder.getAssertion().getPrincipal().getAttributes().get("email").toString();
        String address =   AssertionHolder.getAssertion().getPrincipal().getAttributes().get("address").toString();
        logger.info("4、id邮箱和地址:"+id+"|"+email+"|"+address);

        return  "clientpac4j";
    }

    @GetMapping("threelogin")
    @ResponseBody
    public String github(HttpServletRequest request){

        if (request.getUserPrincipal() != null) {
            AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();

            final Map attributes = principal.getAttributes();


            if (attributes != null) {
                Iterator attributeNames = attributes.keySet().iterator();

                if (attributeNames.hasNext()) {

                    for (; attributeNames.hasNext(); ) {
                        String name = (String) attributeNames.next();
                        System.out.printf("name:"+name+"-------");

                        final Object attributeValue = attributes.get(name);

                        if (attributeValue instanceof List) {
                            final List values = (List) attributeValue;
                            for (Object value : values) {
                                System.out.printf("value:"+value+"|");

                            }
                            System.out.printf("|");
                        } else {
                            System.out.printf(attributeValue.toString());
                        }
                        System.out.println("------------");
                    }
                } else {
                    System.out.println("No attributes are supplied by the CAS server.</p>");
                }
            } else {
                System.out.println("<pre>The attribute map is empty. Review your CAS filter configurations.</pre>");
            }
        } else {
            System.out.println("<pre>The user principal is empty from the request object. Review the wrapper filter configuration.</pre>");
        }


        String user3 = AssertionHolder.getAssertion().getPrincipal().getName();
        System.out.println("3、AssertionHolder.getAssertion().getPrincipal().getName()"+user3);

        return  "github";
    }


    @GetMapping("limitGetAttribute")
    @ResponseBody
    public String limitGetAttribute(HttpServletRequest request){

        if (request.getUserPrincipal() != null) {
            AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();

            final Map attributes = principal.getAttributes();


            if (attributes != null) {
                Iterator attributeNames = attributes.keySet().iterator();

                if (attributeNames.hasNext()) {

                    for (; attributeNames.hasNext(); ) {
                        String name = (String) attributeNames.next();
                        System.out.printf("name:"+name+"-------");

                        final Object attributeValue = attributes.get(name);

                        if (attributeValue instanceof List) {
                            final List values = (List) attributeValue;
                            for (Object value : values) {
                                System.out.printf("value:"+value+"|");

                            }
                            System.out.printf("|");
                        } else {
                            System.out.printf(attributeValue.toString());
                        }
                        System.out.println("------------");
                    }
                } else {
                    System.out.println("No attributes are supplied by the CAS server.</p>");
                }
            } else {
                System.out.println("<pre>The attribute map is empty. Review your CAS filter configurations.</pre>");
            }
        } else {
            System.out.println("<pre>The user principal is empty from the request object. Review the wrapper filter configuration.</pre>");
        }


        String user3 = AssertionHolder.getAssertion().getPrincipal().getName();
        System.out.println("3、AssertionHolder.getAssertion().getPrincipal().getName()"+user3);

        return  "github";
    }


}
