package com.hlj.redis.redisTool;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 类描述：
 * redis操作Long类型数据的工具类
 * 提供Long类型原子操作
 * 默认一天过期,其他过期时间调用setExpire
 * 创建人： j.sh
 * 创建时间： 2016/3/1
 * version：1.0.0
 */
@Component
public class RedisLongData implements InitializingBean {

    @Resource(name="redisTemplate")
    private ValueOperations<String, Long> valueOperations;

    @Resource(name="redisTemplate")
    private RedisTemplate<String,Long> redisTemplate;


    @Override
    public void afterPropertiesSet() throws Exception {
        redisTemplate.setValueSerializer(new GenericToStringSerializer<Long>(Long.class));
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
     * 进行数值的增加
     * @param key
     * @param value
     * @return
     */
    public Long increase(String key,long value){
        Long result = valueOperations.increment(key,value);
        this.setExpire(key,1L,TimeUnit.DAYS);
        return result;
    }

    /**
     * 进行数值的增加
     * @param key
     * @return
     */
    public Long increase(String key){
        return increase(key,1);
    }

    /**
     * 进行数值的递减
     * @param key
     * @param value
     * @return
     */
    public Long decrease(String key,long value){
        Long result =  valueOperations.increment(key,0-value);
        this.setExpire(key,1L,TimeUnit.DAYS);
        return result;
    }

    /**
     * 进行数值的递减
     * @param key
     * @return
     */
    public Long decrease(String key){
        return decrease(key,1);
    }

    /**
     * 根据key获取
     * @param key
     * @return
     */
    public Long get(String key) {
        return valueOperations.get(key);
    }

    /**
     * 设置
     * @param key
     * @param value
     */
    public void set(String key,Long value) {
        valueOperations.set(key,value);
        this.setExpire(key,1L,TimeUnit.DAYS);
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
