package com.hlj.redis.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/30  下午4:55.
 */
@RestController
public class OptsForSetController {

    @Resource(name="stringRedisTemplate")
    private StringRedisTemplate  stringRedisTemplate;


    @Resource(name = "redisTemplate")
    private  RedisTemplate redisTemplate;

    private  String  KEY = "stringRedisTemplate";

    @GetMapping("opsForSet")
    public String set(){
        stringRedisTemplate.opsForSet().add(KEY,"stringRedisTemplate.opsForSet().add");
        stringRedisTemplate.opsForSet().add(KEY,"stringRedisTemplate.opsForSet().add");

        return   stringRedisTemplate.opsForValue().get(KEY).toString();
    }


}
