package com.ctrip.apollo.biz.message;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RedisMessageSender implements MessageSender {
  private RedisTemplate<String, String> redisTemplate;

  public RedisMessageSender(
      RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void sendMessage(String message, String channel) {
    try {
      redisTemplate.convertAndSend(channel, message);
    } catch (Throwable ex) {

    } finally {

    }
  }
}
