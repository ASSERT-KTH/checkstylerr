package com.hlj.netty.websocket.redis.redisTool;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 类描述：
 * 操作redis 对象数据的工具类
 * 提供存取数字外对象的存取，数字类型使用RedisLongData/RedisIntegerData进行操作
 * 这个类分不清Long和Integer,Float和Double。也无法进行原子操作
 * 创建人： j.sh
 * 创建时间： 2016/3/1
 * version：1.0.0
 */
@Component
public class RedisObjectData {

    @Resource(name="redisTemplate")
    private ValueOperations<String, Object> valueOperations;

    @Resource(name = "redisTemplate")
    private RedisOperations<String,Object> operations;

    /**
     * 根据key获取数据
     * @param key
     * @return
     */
    public Object getData(String key) {
        return valueOperations.get(key);
    }

    /**
     * 设置数据
     * @param key
     * @param object
     */
    public void setData(String key,Object object) {
        valueOperations.set(key,object);
    }

    /**
     * 根据key 删除对应的数据
     * @param key
     */
    public void delete(String key) {
        operations.delete(key);
    }
}
