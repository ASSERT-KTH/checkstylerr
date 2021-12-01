package com.ctrip.framework.apollo.openapi.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.openapi.entity.ConsumerAudit;
import com.ctrip.framework.apollo.openapi.service.ConsumerService;
import com.dianping.cat.Cat;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerAuditUtil implements InitializingBean {
  private static final int CONSUMER_AUDIT_MAX_SIZE = 10000;
  private BlockingQueue<ConsumerAudit> audits = Queues.newLinkedBlockingQueue(CONSUMER_AUDIT_MAX_SIZE);
  private final ExecutorService auditExecutorService;
  private final AtomicBoolean auditStopped;

  @Autowired
  private ConsumerService consumerService;

  public ConsumerAuditUtil() {
    auditExecutorService = Executors.newSingleThreadExecutor(
        ApolloThreadFactory.create("ConsumerAuditUtil", true));
    auditStopped = new AtomicBoolean(false);
  }

  public boolean audit(HttpServletRequest request, long consumerId) {
    String uri = request.getRequestURI();
    if (!Strings.isNullOrEmpty(request.getQueryString())) {
      uri += "?" + request.getQueryString();
    }

    ConsumerAudit consumerAudit = new ConsumerAudit();
    Date now = new Date();
    consumerAudit.setConsumerId(consumerId);
    consumerAudit.setUri(uri);
    consumerAudit.setMethod(request.getMethod());
    consumerAudit.setDataChangeCreatedTime(now);
    consumerAudit.setDataChangeLastModifiedTime(now);

    //throw away audits if exceeds the max size
    return this.audits.offer(consumerAudit);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    auditExecutorService.submit(() -> {
      while (!auditStopped.get() && !Thread.currentThread().isInterrupted()) {
        List<ConsumerAudit> toAudit = Lists.newArrayList();
        try {
          Queues.drain(audits, toAudit, 100, 5, TimeUnit.SECONDS);
          if (!toAudit.isEmpty()) {
            consumerService.createConsumerAudits(toAudit);
          }
        } catch (Throwable ex) {
          Cat.logError(ex);
        }
      }
    });
  }
}
