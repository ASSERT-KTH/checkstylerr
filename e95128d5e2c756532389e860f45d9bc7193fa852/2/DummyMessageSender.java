package com.ctrip.apollo.biz.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class DummyMessageSender implements MessageSender{
  private static final Logger logger = LoggerFactory.getLogger(DummyMessageSender.class);
  @Override
  public void sendMessage(String message, String channel) {
    logger.warn("No message sender available! message: {}, channel: {}", message, channel);
  }
}
