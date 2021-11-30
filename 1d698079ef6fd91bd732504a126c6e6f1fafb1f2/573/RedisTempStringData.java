package com.hlj.redis.redisTool;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 类描述：reids临时字符串变量
 * 创建人： qingxu
 * 创建时间： 2016/7/18
 * version：1.0.0
 */
@Repository
public class RedisTempStringData {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private StringRedisSerializer stringRedisSerializer;


    @PostConstruct
    public void init() {
        stringRedisSerializer = new StringRedisSerializer();
    }

    public String getValue(final String key){
        return stringRedisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] keys = stringRedisSerializer.serialize(key);
                byte[] values = redisConnection.get(keys);
                return stringRedisSerializer.deserialize(values);
            }
        });
    }


    public boolean  setValue(final String key,final String value,final Expiration expiration) {
        return stringRedisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] keys = stringRedisSerializer.serialize(key);
                byte[] values = stringRedisSerializer.serialize(value);
                redisConnection.set(keys,values, expiration, RedisStringCommands.SetOption.UPSERT);
                return true;
            }
        });
    }


}
