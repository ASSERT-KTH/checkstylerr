package com.hlj.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.net.UnknownHostException;

/**
 * @author fengchuanbo
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(@Qualifier("redisWithTemplate") RedisTemplate redisTemplate) {
        RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
        // 多个缓存的名称,目前只定义了一个
        //设置缓存过期时间(秒)
        rcm.setDefaultExpiration(1800);
        return rcm;
    }

    @Primary
    @Bean(name = {"admoreRedisTemplate", "redisTemplate"})
    public RedisTemplate<Object, Object> commonRedisTemplate(@Qualifier("admoreRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        return redisTemplate(redisConnectionFactory);
    }

    @Primary
    @Bean(name = {"admoreRedisConnectionFactory", "commonRedisConnectionFactory"})
    @ConfigurationProperties("hlj.redis")
    public RedisConnectionFactory admoreRedisConnectionFactory(RedisProperties redisProperties) {
        return createJedisConnectionFactory(redisProperties);
    }

    @Primary
    @Bean(name = {"admoreStringRedisTemplate", "stringRedisTemplate"})
    public StringRedisTemplate admoreStringRedisTemplate(@Qualifier("admoreRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    private JedisConnectionFactory createJedisConnectionFactory(RedisProperties redisProperties) {
        JedisPoolConfig poolConfig = redisProperties.getPool() != null ? jedisPoolConfig(redisProperties) : new JedisPoolConfig();
        return new JedisConnectionFactory(poolConfig);
    }

    private JedisPoolConfig jedisPoolConfig(RedisProperties redisProperties) {
        JedisPoolConfig config = new JedisPoolConfig();
        RedisProperties.Pool props = redisProperties.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        return config;
    }

    private RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Primary
    @Bean(name = {"redisWithTemplate"})
    public RedisTemplate<String, String> redisWithTemplate(@Qualifier("admoreRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        return template;
    }
}
