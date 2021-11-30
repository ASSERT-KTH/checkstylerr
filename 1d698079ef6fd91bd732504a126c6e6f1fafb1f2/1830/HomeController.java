package com.didispace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HomeController {


    @Autowired
    private DiscoveryClient client;

    @RequestMapping(value = "hello",method = RequestMethod.GET)
    public  ServiceInstance index(){
        ServiceInstance serviceInstance = client.getLocalServiceInstance();
        System.out.println(
                "host："+serviceInstance.getHost()+"/n " +
                "service_id :"+serviceInstance.getServiceId());
        return serviceInstance;
    }

}
