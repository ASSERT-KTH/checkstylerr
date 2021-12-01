package com.ctrip.apollo.biz.message;

import com.google.common.collect.Lists;

import com.ctrip.apollo.biz.entity.ReleaseMessage;
import com.ctrip.apollo.biz.repository.ReleaseMessageRepository;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ReleaseMessageScanner implements InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(ReleaseMessageScanner.class);
  private static final int DEFAULT_SCAN_INTERVAL_IN_MS = 1000;
  @Autowired
  private Environment env;
  @Autowired
  private ReleaseMessageRepository releaseMessageRepository;
  private int databaseScanInterval;
  private List<MessageListener> listeners;
  private ScheduledExecutorService executorService;
  private long maxIdScanned;

  public ReleaseMessageScanner() {
    listeners = Lists.newLinkedList();
    executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory
        .create("ReleaseMessageScanner", true));
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    populateDataBaseInterval();
    maxIdScanned = loadLargestMessageId();
    executorService.scheduleWithFixedDelay((Runnable) () -> {
      Transaction transaction = Cat.newTransaction("Apollo.ReleaseMessageScanner", "scanMessage");
      try {
        scanMessages();
        transaction.setStatus(Message.SUCCESS);
      } catch (Throwable ex) {
        transaction.setStatus(ex);
        logger.error("Scan and send message failed", ex);
      } finally {
        transaction.complete();
      }
    }, getDatabaseScanIntervalMs(), getDatabaseScanIntervalMs(), TimeUnit.MILLISECONDS);

  }

  /**
   * add message listeners for release message
   * @param listener
   */
  public void addMessageListener(MessageListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Scan messages, continue scanning until there is no more messages
   */
  private void scanMessages() {
    boolean hasMoreMessages = true;
    while (hasMoreMessages && !Thread.currentThread().isInterrupted()) {
      hasMoreMessages = scanAndSendMessages();
    }
  }

  /**
   * scan messages and send
   *
   * @return whether there are more messages
   */
  private boolean scanAndSendMessages() {
    //current batch is 500
    List<ReleaseMessage> releaseMessages =
        releaseMessageRepository.findFirst500ByIdGreaterThanOrderByIdAsc(maxIdScanned);
    if (CollectionUtils.isEmpty(releaseMessages)) {
      return false;
    }
    fireMessageScanned(releaseMessages);
    int messageScanned = releaseMessages.size();
    maxIdScanned = releaseMessages.get(messageScanned - 1).getId();
    return messageScanned == 500;
  }

  /**
   * find largest message id as the current start point
   * @return current largest message id
   */
  private long loadLargestMessageId() {
    ReleaseMessage releaseMessage = releaseMessageRepository.findTopByOrderByIdDesc();
    return releaseMessage == null ? 0 : releaseMessage.getId();
  }

  /**
   * Notify listeners with messages loaded
   * @param messages
   */
  private void fireMessageScanned(List<ReleaseMessage> messages) {
    for (ReleaseMessage message : messages) {
      for (MessageListener listener : listeners) {
        try {
          listener.handleMessage(message.getMessage(), Topics.APOLLO_RELEASE_TOPIC);
        } catch (Throwable ex) {
          Cat.logError(ex);
          logger.error("Failed to invoke message listener {}", listener.getClass(), ex);
        }
      }
    }
  }

  private void populateDataBaseInterval() {
    databaseScanInterval = DEFAULT_SCAN_INTERVAL_IN_MS;
    try {
      String interval = env.getProperty("apollo.message-scan.interval");
      if (!Objects.isNull(interval)) {
        databaseScanInterval = Integer.parseInt(interval);
      }
    } catch (Throwable ex) {
      Cat.logError(ex);
      logger.error("Load apollo message scan interval from system property failed", ex);
    }
  }

  private int getDatabaseScanIntervalMs() {
    return databaseScanInterval;
  }
}
