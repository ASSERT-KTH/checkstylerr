package com.didispace.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConsumerController {


    @Autowired
    RestTemplate restTemplate;

    /**
     * 1、url获取String
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer", method = RequestMethod.GET)
    public String helloConsumer() {
        return restTemplate.getForEntity("http://HELLO-SERVICE/hello?str=healejean",String.class).getBody();
    }

    /**
     * 2、对象和占位符{}
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer_user", method = RequestMethod.GET)
    public User userUrl() {
        ResponseEntity<User> responseEntity =  restTemplate.getForEntity("http://HELLO-SERVICE/user_url?name={1}",User.class,"HealerJean");
        User body = responseEntity.getBody();
        return  body;
    }

    /**
     * map展位
     */

    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer_userMap", method = RequestMethod.GET)
    public User userUrlMap() {
        Map<String,Object> map = new HashMap<>();
        map.put("name","HealerJean");
        ResponseEntity<User> responseEntity =  restTemplate.getForEntity("http://HELLO-SERVICE/user_url?name={name}",User.class,map);
        User body = responseEntity.getBody();
        return  body;
    }


    /**
     * 4、get的第二中请求方式
     */

    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer_getTwo", method = RequestMethod.GET)
    public User getTwo() {
        User body   =  restTemplate.getForObject("http://HELLO-SERVICE/user_url?name={1}",User.class,"HealerJean");
        return  body;
    }


    /**
     * 5、post请求
     */

    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer_postFirst", method = RequestMethod.GET)
    public User postFirst() {
        User user = new User();
        user.setName("HealerJean");
        ResponseEntity<User> responseEntity   =  restTemplate.postForEntity("http://HELLO-SERVICE/user_urlPost",user,User.class);
        User body = responseEntity.getBody();
        return  body;
    }


    /**
     * 6、put请求
     */

    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer_put", method = RequestMethod.GET)
    public User put() {
        User user = new User();
        user.setName("HealerJean");
        String id= "2";

         restTemplate.put("http://HELLO-SERVICE/user_url/{1}",user,id);
        return  user;
    }

    /**
     * delete 请求
     */
    @ResponseBody
    @RequestMapping(value = "/ribbon-consumer_delete", method = RequestMethod.GET)
    public User delete() {
        String id= "2";
        restTemplate.delete("http://HELLO-SERVICE/user_url/{1}",id);
        return  null;
    }
}
