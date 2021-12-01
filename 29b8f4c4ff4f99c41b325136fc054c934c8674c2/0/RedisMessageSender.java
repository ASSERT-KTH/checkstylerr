package com.ctrip.apollo.biz.message;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class RedisMessageSender implements MessageSender {
  private static final Logger logger = LoggerFactory.getLogger(RedisMessageSender.class);
  private RedisTemplate<String, String> redisTemplate;

  public RedisMessageSender(
      RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void sendMessage(String message, String channel) {
    logger.info("Sending message {} to channel {}", message, channel);
    Transaction transaction = Cat.newTransaction("Apollo.AdminService", "RedisMessageSender");
    try {
      redisTemplate.convertAndSend(channel, message);
      transaction.setStatus(Message.SUCCESS);
    } catch (Throwable ex) {
      logger.error("Sending message to redis failed", ex);
      transaction.setStatus(ex);
    } finally {
      transaction.complete();
    }
  }
}
