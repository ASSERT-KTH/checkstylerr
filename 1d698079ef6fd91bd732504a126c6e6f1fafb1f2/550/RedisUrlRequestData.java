package com.hlj.redis.redisTool;

import com.hlj.redis.EnumRedisIndex;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 类描述：用以拦截用户重复请求
 * 创建人： j.sh
 * 创建时间： 2016/3/1
 * version：1.0.0
 */
@Component
public class RedisUrlRequestData {

    private static Integer DB_INDEX = EnumRedisIndex.普通临时变量.index;

    @Resource
    private RedisTemplate<String,Long> redisTemplate;

    private StringRedisSerializer stringRedisSerializer;
    private GenericToStringSerializer longResisSerializer;

    @PostConstruct
    private void afterPropertiesSet() throws Exception {
        stringRedisSerializer = new StringRedisSerializer();
    }

    /**
     * 请求次数
     * @param key
     * @return
     */
    public Long increase(final String key,final Long seconds){
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.select(DB_INDEX);
                byte[] keyBytes = stringRedisSerializer.serialize(key);

                Long val = redisConnection.incr(keyBytes);
                redisConnection.expire(keyBytes,seconds);
                return val;
            }
        });
    }

}
