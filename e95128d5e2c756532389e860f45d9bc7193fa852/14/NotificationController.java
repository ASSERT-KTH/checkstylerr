package com.ctrip.apollo.configservice.controller;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.ctrip.apollo.biz.message.MessageListener;
import com.ctrip.apollo.biz.message.Topics;
import com.ctrip.apollo.core.dto.ApolloConfigNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController implements MessageListener {
  private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
  private final static long TIMEOUT = 360 * 60 * 1000;//6 hours
  private final Multimap<String, DeferredResult<ApolloConfigNotification>> deferredResults =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  @RequestMapping(method = RequestMethod.GET)
  public DeferredResult<ApolloConfigNotification> pollNotification(
      @RequestParam(value = "appId") String appId,
      @RequestParam(value = "cluster") String cluster,
      @RequestParam(value = "namespace", required = false) String namespace,
      @RequestParam(value = "datacenter", required = false) String datacenter,
      @RequestParam(value = "releaseId", defaultValue = "-1") String clientSideReleaseId,
      HttpServletResponse response) {
    //check default namespace
    if (Objects.isNull(namespace)) {
      namespace = appId;
    }

    List<String> watchedKeys = Lists.newArrayList(assembleKey(appId, cluster, namespace));

    //Listen more namespaces, since it's not the default namespace
    if (!Objects.equals(appId, namespace)) {
      //TODO find id for this particular namespace, if not equal to current app id, then do more
      if (!Objects.isNull(datacenter)) {
        //TODO add newAppId+datacenter+namespace to listened keys
      }
      //TODO add newAppId+defaultCluster+namespace to listened keys
    }

    DeferredResult<ApolloConfigNotification> deferredResult =
        new DeferredResult<>(TIMEOUT);

    //register all keys
    for (String key : watchedKeys) {
      this.deferredResults.put(key, deferredResult);
    }

    deferredResult.onCompletion(() -> {
      //unregister all keys
      for (String key : watchedKeys) {
        deferredResults.remove(key, deferredResult);
      }
    });

    deferredResult.onTimeout(() -> {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    });

    return deferredResult;
  }

  private String assembleKey(String appId, String cluster, String namespace) {
    return String.format("%s-%s-%s", appId, cluster, namespace);
  }

  @Override
  public void handleMessage(String message, String channel) {
    logger.info("message received - channel: {}, message: {}", channel, message);
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(message)) {
      return;
    }
    String[] keys = message.split("-");
    //message should be appId-cluster-namespace
    if (keys.length != 3) {
      logger.error("message format invalid - {}", message);
      return;
    }

    ApolloConfigNotification notification = new ApolloConfigNotification(keys[0], keys[1], keys[2]);

    Collection<DeferredResult<ApolloConfigNotification>> results = deferredResults.get(message);
    logger.info("Notify {} clients for key {}", results.size(), message);

    for (DeferredResult<ApolloConfigNotification> result : results) {
      result.setResult(notification);
    }
  }
}

