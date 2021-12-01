package com.ctrip.apollo.adminservice;

import com.ctrip.apollo.biz.message.DummyMessageSender;
import com.ctrip.apollo.biz.message.MessageSender;
import com.ctrip.apollo.biz.message.RedisMessageSender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
public class AdminServiceAutoConfiguration {
  @ConditionalOnProperty(value = "apollo.redis.enabled", havingValue = "true", matchIfMissing = false)
  public static class AdminRedisConfiguration {
    @Value("${apollo.redis.host}")
    private String host;
    @Value("${apollo.redis.port}")
    private int port;
    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
      JedisConnectionFactory factory = new JedisConnectionFactory();
      factory.setHostName(host);
      factory.setPort(port);
      return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
      StringRedisTemplate template = new StringRedisTemplate(factory);
      return template;
    }

    @Bean
    public MessageSender redisMessageSender(RedisTemplate<String, String> redisTemplate) {
      return new RedisMessageSender(redisTemplate);
    }

  }

  @Configuration
  @ConditionalOnProperty(value = "apollo.redis.enabled", havingValue = "false", matchIfMissing = true)
  public static class ConfigDefaultConfiguration {
    @Bean
    public MessageSender defaultMessageSender() {
      return new DummyMessageSender();
    }
  }
}
