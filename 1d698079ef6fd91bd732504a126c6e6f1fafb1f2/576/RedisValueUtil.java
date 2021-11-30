package com.hlj.redis.redisTool.tools;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Component
public class RedisValueUtil {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 根据key获取数据
     * @param key
     * @return
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置数据
     * @param key key值
     * @param object 对象
     */
    public void set(String key,Object object) {
        redisTemplate.opsForValue().set(key,object);
    }


    /**
     * 设置数据 并且添加过期时间
     * @param key key值
     * @param object 对象
     * @param time 过期时间
     * @param timeUnit 过期时间的格式
     */
    public void set(String key, Object object, Long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key,object,time,timeUnit);
    }


    /**
     * 根据key 删除对应的数据
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
