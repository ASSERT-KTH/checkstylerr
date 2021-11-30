package com.hlj.redis.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/2  上午11:16.
 */
@RestController
public class SerializerController {

    @Resource(name="stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;


    private  String  KEY = "stringRedisTemplate";

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();


    @GetMapping("serialize")
    public String serialize(){

        return stringRedisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                //选中哪一个库
             //   redisConnection.select();
                byte[] keys = stringRedisSerializer.serialize(KEY);
                byte[] values = redisConnection.get(keys);
                return stringRedisSerializer.deserialize(values);
            }
        });
    }


    //自增
    @GetMapping("increase")
    public Long increase(){
        return stringRedisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] keyBytes = stringRedisSerializer.serialize("long");
                Long val = redisConnection.incr(keyBytes);
                redisConnection.expire(keyBytes,100L);
                return val;
            }
        });
    }

}
