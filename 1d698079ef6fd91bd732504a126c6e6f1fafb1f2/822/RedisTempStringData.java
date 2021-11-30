package com.hlj.netty.websocket.redis.redisTool;

import com.hlj.netty.websocket.redis.EnumRedisIndex;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
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

    private static Integer DB_INDEX = EnumRedisIndex.普通临时变量.index;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    private StringRedisSerializer stringRedisSerializer;


    @PostConstruct
    public void init() {
        stringRedisSerializer = new StringRedisSerializer();
    }

    public String getValue(final String key){
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.select(DB_INDEX);

                byte[] keys = stringRedisSerializer.serialize(key);
                byte[] values = redisConnection.get(keys);
                return stringRedisSerializer.deserialize(values);
            }
        });
    }

    public boolean  setValue(final String key,final String value) {
        return setValue(key, value, Expiration.from(7,TimeUnit.DAYS));
    }

    public boolean  setValue(final String key,final String value,final Expiration expiration) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.select(DB_INDEX);

                byte[] keys = stringRedisSerializer.serialize(key);
                byte[] values = stringRedisSerializer.serialize(value);
                redisConnection.set(keys,values, expiration, RedisStringCommands.SetOption.UPSERT);
                return true;
            }
        });
    }

}
