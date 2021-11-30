package com.hlj.redis.controller.tools;

import com.hlj.redis.redisTool.tools.RedisListUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/11  下午2:16.
 * 类描述：
 */
@RestController
@RequestMapping("redis/list")
public class RedisListUtilController {



    @Resource
    private   RedisListUtil redisListUtil ;

    //http://localhost:8080/redis/list/lpush?key=mylist&val1=a&val2=b&val3=c
    @GetMapping("lpush")
    public Object hashPut(String key,String val1,String val2,String val3){
        redisListUtil.lpush(key,val1,val2,val3);
        return  "ok";
    }

    @GetMapping("rpush")
    public Object rpush(String key,String val1,String val2,String val3){
        redisListUtil.rpush(key,val1,val2,val3);
        return  "ok";
    }


    @GetMapping("brpop")
    public Object rpush(String key){
        return redisListUtil.brpop(key,1L, TimeUnit.MINUTES);
    }

    @GetMapping("blpop")
    public Object blpop(String key){
       return   redisListUtil.blpop(key,1L, TimeUnit.MINUTES);
    }

    @GetMapping("rpop")
    public Object rpop(String key){
        return redisListUtil.rpop(key);
    }

    @GetMapping("lpop")
    public Object lpop(String key){
        return redisListUtil.lpop(key);
    }

}
