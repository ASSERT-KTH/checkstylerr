package com.hlj.redis.topiclistener.controller;

import com.hlj.redis.topiclistener.data.ConvertBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/13  下午1:30.
 */
@RequestMapping("redis/listener")
@Controller
public class ListenerController {

    @Resource
    private  RedisTemplate redisTemplate;

    @GetMapping("test")
    @ResponseBody
    public void lockRedis(){
        ConvertBean convertBean = new ConvertBean();
        convertBean.setContent("content");
        convertBean.setToUid("uuid");

        redisTemplate.convertAndSend("request",convertBean);

    }

}
