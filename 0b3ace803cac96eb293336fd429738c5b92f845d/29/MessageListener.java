package com.ctrip.framework.apollo.biz.message;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface MessageListener {
  void handleMessage(String message, String channel);
}
