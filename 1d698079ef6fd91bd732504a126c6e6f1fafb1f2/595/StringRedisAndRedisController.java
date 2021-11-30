package com.hlj.redis.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/11  下午2:39.
 * 类描述：
 */
@RestController
@RequestMapping("redis/string/redis")
public class StringRedisAndRedisController {


    @Resource
    private StringRedisTemplate  stringRedisTemplate ;//默认就是String类型的

    @Resource
    private RedisTemplate redisTemplate ; //这种存储方式会造成key乱码(不要使用，而且返回结果会报错)

    @Resource
    private RedisTemplate<String,String> redisWithTemplate  ; //各自管理各自的


    //http://localhost:8080/redis/string/redis/stringget?key=key
    @GetMapping("stringadd")
    public String stringadd(String key,String value){
        stringRedisTemplate.opsForValue().set(key,value);
        redisTemplate.opsForValue().set(key+1,value+1 );
        redisWithTemplate.opsForValue().set(key+2,value+2 );
        return "ok";
    }

    //http://localhost:8080/redis/string/redis/stringget?key=key
    @GetMapping("stringget")
    public String stringget(String key){

//        System.out.println(stringRedisTemplate.opsForValue().get(key)); //value
//        System.out.println(stringRedisTemplate.opsForValue().get(key+1));//null
//        System.out.println(stringRedisTemplate.opsForValue().get(key+2));//value2
//        System.out.println("---------");

//        System.out.println(redisTemplate.opsForValue().get(key));//null
//        System.out.println(redisTemplate.opsForValue().get(key+1));
//        System.out.println(redisTemplate.opsForValue().get(key+2));//null
//        System.out.println("---------");

//        System.out.println(redisWithTemplate.opsForValue().get(key));
//        System.out.println(redisWithTemplate.opsForValue().get(key+1));
        System.out.println(redisWithTemplate.opsForValue().get(key+2));

        return "ok";
    }

}
