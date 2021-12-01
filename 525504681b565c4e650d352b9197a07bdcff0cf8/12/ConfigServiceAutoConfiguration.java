package com.ctrip.apollo.configservice;

import com.ctrip.apollo.biz.message.Topics;
import com.ctrip.apollo.configservice.controller.NotificationController;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Configuration
public class ConfigServiceAutoConfiguration {

  @ConditionalOnProperty(value = "apollo.redis.enabled", havingValue = "true", matchIfMissing = false)
  public static class ConfigRedisConfiguration {
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
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory factory) {
      RedisMessageListenerContainer container = new RedisMessageListenerContainer();
      container.setConnectionFactory(factory);
      return container;
    }

    @Bean
    public ChannelTopic apolloReleaseTopic() {
      return new ChannelTopic(Topics.APOLLO_RELEASE_TOPIC);
    }

    @Bean
    public MessageListenerAdapter apolloMessageListener(RedisMessageListenerContainer container,
                                                        NotificationController notification,
                                                        ChannelTopic topic) {
      MessageListenerAdapter adapter = new MessageListenerAdapter(notification);
      container.addMessageListener(adapter, topic);
      return adapter;
    }
  }
}
