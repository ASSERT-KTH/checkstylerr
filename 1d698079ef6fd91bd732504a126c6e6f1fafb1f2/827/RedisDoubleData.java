package com.hlj.netty.websocket.redis.redisTool;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 类描述：
 * redis操作Double类型数据的工具类
 * 提供double 数字存储
 * 默认一天过期,其他过期时间调用setExpire
 * 创建人： j.sh
 * 创建时间： 2016/3/1
 * version：1.0.0
 */
@Component
public class RedisDoubleData implements InitializingBean {

    @Resource(name="redisTemplate")
    private ValueOperations<String, Double> valueOperations;

    @Resource(name="redisTemplate")
    private RedisTemplate<String,Double> redisTemplate;


    @Override
    public void afterPropertiesSet() throws Exception {
        redisTemplate.setValueSerializer(new GenericToStringSerializer<Double>(Double.class));
        valueOperations = redisTemplate.opsForValue();
    }

    /**
     * 根据key 删除
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 根据key获取
     * @param key
     * @return
     */
    public Double get(String key) {
        return valueOperations.get(key);
    }

    /**
     * 设置
     * @param key
     * @param value
     */
    public void set(String key,Double value) {
        valueOperations.set(key,value);
    }

    /**
     * 过期时间，默认单位秒
     * @param key
     * @param time
     */
    public void setExpire(String key,Long time){
        redisTemplate.expire(key,time, TimeUnit.SECONDS);
    }

    /**
     * 过期时间
     * @param key
     * @param time
     * @param timeUnit
     */
    public void setExpire(String key,Long time,TimeUnit timeUnit){
        redisTemplate.expire(key,time,timeUnit);
    }

}
