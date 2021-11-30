package com.didispace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HomeController {


    @Autowired
    private DiscoveryClient client;

    @RequestMapping(value = "hello",method = RequestMethod.GET)
    @ResponseBody
    public  String index(String str){
        ServiceInstance serviceInstance = client.getLocalServiceInstance();
        System.out.println(
                "host："+serviceInstance.getHost()+"/n " +
                        "service_id :"+serviceInstance.getServiceId());
        return str;
    }

    @RequestMapping(value = "user_url",method = RequestMethod.GET)
    @ResponseBody
    public  User index(User user){
        ServiceInstance serviceInstance = client.getLocalServiceInstance();
        System.out.println(
                "host："+serviceInstance.getHost()+"/n " +
                        "service_id :"+serviceInstance.getServiceId());

        user.setId("1");
        return user;
    }

    @RequestMapping(value = "user_urlPost",method = RequestMethod.POST)
    @ResponseBody
    public  User urlPost(User user){
        ServiceInstance serviceInstance = client.getLocalServiceInstance();
        System.out.println(
                "host："+serviceInstance.getHost()+"/n " +
                        "service_id :"+serviceInstance.getServiceId());

        user.setId("1");
        return user;
    }


}
