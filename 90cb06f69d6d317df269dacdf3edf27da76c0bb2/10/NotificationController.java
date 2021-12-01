package com.ctrip.apollo.configservice.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.apollo.core.utils.ApolloThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
  private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
  private final static long TIMEOUT = 120 * 60 * 1000;//120 MINUTES
  private final Multimap<String, DeferredResult<ApolloConfigNotification>> deferredResults =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());
  private final Multimap<DeferredResult<ApolloConfigNotification>, String> deferredResultReversed =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  {
    startRandomChange();
  }

  @RequestMapping(method = RequestMethod.GET)
  public DeferredResult<ApolloConfigNotification> pollNotification(
      @RequestParam(value = "appId") String appId,
      @RequestParam(value = "cluster") String cluster,
      @RequestParam(value = "namespace", defaultValue = ConfigConsts.NAMESPACE_APPLICATION) String namespace,
      @RequestParam(value = "datacenter", required = false) String datacenter,
      @RequestParam(value = "releaseId", defaultValue = "-1") String clientSideReleaseId,
      HttpServletResponse response) {
    DeferredResult<ApolloConfigNotification> deferredResult =
        new DeferredResult<>(TIMEOUT);
    String key = assembleKey(appId, cluster, namespace);
    this.deferredResults.put(key, deferredResult);
    //to record all the keys related to deferredResult
    this.deferredResultReversed.put(deferredResult, key);

    deferredResult.onCompletion(() -> {
      logger.info("deferred result for {} {} {} completed", appId, cluster, namespace);
      deferredResults.remove(key, deferredResult);
    });

    deferredResult.onTimeout(() -> {
      logger.info("deferred result for {} {} {} timeout", appId, cluster, namespace);
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    });

    logger.info("deferred result for {} {} {} returned", appId, cluster, namespace);
    return deferredResult;
  }

  private void startRandomChange() {
    Random random = new Random();
    ScheduledExecutorService testService = Executors.newScheduledThreadPool(1,
        ApolloThreadFactory.create("NotificationController", true));
    testService.scheduleAtFixedRate((Runnable) () -> deferredResults
        .entries().stream().filter(entry -> random.nextBoolean()).forEach(entry -> {
          String[] keys = entry.getKey().split("-");
          entry.getValue().setResult(new ApolloConfigNotification(keys[0], keys[1], keys[2]));
        }), 30, 30, TimeUnit.SECONDS);
  }

  private String assembleKey(String appId, String cluster, String namespace) {
    return String.format("%s-%s-%s", appId, cluster, namespace);
  }
}

