package com.ctrip.apollo.configservice.controller;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.message.MessageListener;
import com.ctrip.apollo.biz.message.Topics;
import com.ctrip.apollo.biz.service.AppNamespaceService;
import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.dto.ApolloConfigNotification;
import com.dianping.cat.Cat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  private static final long TIMEOUT = 360 * 60 * 1000;//6 hours
  private final Multimap<String, DeferredResult<ResponseEntity<ApolloConfigNotification>>>
      deferredResults =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  @Autowired
  private AppNamespaceService appNamespaceService;

  @RequestMapping(method = RequestMethod.GET)
  public DeferredResult<ResponseEntity<ApolloConfigNotification>> pollNotification(
      @RequestParam(value = "appId") String appId,
      @RequestParam(value = "cluster") String cluster,
      @RequestParam(value = "namespace", defaultValue = ConfigConsts.NAMESPACE_DEFAULT) String namespace,
      @RequestParam(value = "dataCenter", required = false) String dataCenter,
      @RequestParam(value = "releaseId", defaultValue = "-1") String clientSideReleaseId,
      HttpServletResponse response) {
    List<String> watchedKeys = Lists.newArrayList(assembleKey(appId, cluster, namespace));

    //Listen on more namespaces, since it's not the default namespace
    if (!Objects.equals(ConfigConsts.NAMESPACE_DEFAULT, namespace)) {
      watchedKeys.addAll(this.findPublicConfigWatchKey(appId, namespace, dataCenter));
    }

    ResponseEntity<ApolloConfigNotification> body = new ResponseEntity<>(
        HttpStatus.NOT_MODIFIED);
    DeferredResult<ResponseEntity<ApolloConfigNotification>> deferredResult =
        new DeferredResult<>(TIMEOUT, body);

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

    logger.info("Listening {} from appId: {}, cluster: {}, namespace: {}, datacenter: {}",
        watchedKeys, appId, cluster, namespace, dataCenter);
    return deferredResult;
  }

  private String assembleKey(String appId, String cluster, String namespace) {
    return String.format("%s-%s-%s", appId, cluster, namespace);
  }

  private List<String> findPublicConfigWatchKey(String applicationId, String namespace,
                                                String dataCenter) {
    List<String> publicWatchedKeys = Lists.newArrayList();
    AppNamespace appNamespace = appNamespaceService.findByNamespaceName(namespace);

    //check whether the namespace's appId equals to current one
    if (Objects.isNull(appNamespace) || Objects.equals(applicationId, appNamespace.getAppId())) {
      return publicWatchedKeys;
    }

    String publicConfigAppId = appNamespace.getAppId();

    //watch data center config change
    if (!Objects.isNull(dataCenter)) {
      publicWatchedKeys.add(assembleKey(publicConfigAppId, dataCenter, namespace));
    }

    //watch default cluster config change
    publicWatchedKeys
        .add(assembleKey(publicConfigAppId, ConfigConsts.CLUSTER_NAME_DEFAULT, namespace));

    return publicWatchedKeys;
  }

  @Override
  public void handleMessage(String message, String channel) {
    logger.info("message received - channel: {}, message: {}", channel, message);
    Cat.logEvent("Apollo.LongPoll.Message", message);
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(message)) {
      return;
    }
    String[] keys = message.split("-");
    //message should be appId-cluster-namespace
    if (keys.length != 3) {
      logger.error("message format invalid - {}", message);
      return;
    }

    ResponseEntity<ApolloConfigNotification> notification =
        new ResponseEntity<>(
            new ApolloConfigNotification(keys[2]), HttpStatus.OK);

    Collection<DeferredResult<ResponseEntity<ApolloConfigNotification>>>
        results = deferredResults.get(message);
    logger.info("Notify {} clients for key {}", results.size(), message);

    for (DeferredResult<ResponseEntity<ApolloConfigNotification>> result : results) {
      result.setResult(notification);
    }
  }
}

