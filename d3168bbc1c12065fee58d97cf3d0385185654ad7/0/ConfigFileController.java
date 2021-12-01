package com.ctrip.framework.apollo.configservice.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageListener;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtil;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.utils.PropertiesUtil;
import com.dianping.cat.Cat;

import org.hibernate.cache.spi.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/configfiles")
public class ConfigFileController implements ReleaseMessageListener{
  private static final Logger logger = LoggerFactory.getLogger(ConfigFileController.class);
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  private static final long MAX_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
  private static final long EXPIRE_AFTER_WRITE = 10;
  private final HttpHeaders responseHeaders;
  private final ResponseEntity<String> NOT_FOUND_RESPONSE;
  private Cache<String, String> localCache;
  private final Multimap<String, String>
      watchedKeys2CacheKey = Multimaps.synchronizedSetMultimap(HashMultimap.create());
  private final Multimap<String, String>
      cacheKey2WatchedKeys = Multimaps.synchronizedSetMultimap(HashMultimap.create());

  @Autowired
  private ConfigController configController;

  @Autowired
  private NamespaceUtil namespaceUtil;

  @Autowired
  private WatchKeysUtil watchKeysUtil;

  public ConfigFileController() {
    localCache = CacheBuilder.newBuilder()
        .expireAfterWrite(EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
        .weigher(new Weigher<String, String>() {
          @Override
          public int weigh(String key, String value) {
            return value == null ? 0 : value.length();
          }
        })
        .maximumWeight(MAX_CACHE_SIZE)
        .removalListener(new RemovalListener<String, String>() {
          @Override
          public void onRemoval(RemovalNotification<String, String> notification) {
            String cacheKey = notification.getKey();
            logger.debug("removing cache key: {}", cacheKey);
            if (!cacheKey2WatchedKeys.containsKey(cacheKey)) {
              return;
            }
            //create a new list to avoid ConcurrentModificationException
            List<String> watchedKeys = new ArrayList<>(cacheKey2WatchedKeys.get(cacheKey));
            for (String watchedKey : watchedKeys) {
              watchedKeys2CacheKey.remove(watchedKey, cacheKey);
            }
            cacheKey2WatchedKeys.removeAll(cacheKey);
            logger.debug("removed cache key: {}", cacheKey);
          }
        })
        .build();
    responseHeaders = new HttpHeaders();
    responseHeaders.add("Content-Type", "text/plain;charset=UTF-8");
    NOT_FOUND_RESPONSE = new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/{appId}/{clusterName}/{namespace:.+}", method = RequestMethod.GET)
  public ResponseEntity<String> queryConfigAsFile(@PathVariable String appId,
                                                  @PathVariable String clusterName,
                                                  @PathVariable String namespace,
                                                  @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                                  @RequestParam(value = "ip", required = false) String clientIp,
                                                  HttpServletResponse response) throws IOException {
    //strip out .properties suffix
    namespace = namespaceUtil.filterNamespaceName(namespace);

    //TODO add clientIp as key parts?
    String cacheKey = assembleCacheKey(appId, clusterName, namespace, dataCenter);

    String result = localCache.getIfPresent(cacheKey);

    if (Strings.isNullOrEmpty(result)) {
      Cat.logEvent("ConfigFile-Cache-Miss", cacheKey);
      ApolloConfig apolloConfig =
          configController
              .queryConfig(appId, clusterName, namespace, dataCenter, "-1", clientIp,
                  response);

      if (apolloConfig == null || apolloConfig.getConfigurations() == null) {
        return NOT_FOUND_RESPONSE;
      }
      Properties properties = new Properties();
      properties.putAll(apolloConfig.getConfigurations());
      result = PropertiesUtil.toString(properties);

      localCache.put(cacheKey, result);
      logger.debug("adding cache for key: {}", cacheKey);

      Set<String> watchedKeys =
          watchKeysUtil.assembleAllWatchKeys(appId, clusterName, namespace, dataCenter);

      for (String watchedKey : watchedKeys) {
        watchedKeys2CacheKey.put(watchedKey, cacheKey);
      }

      cacheKey2WatchedKeys.putAll(cacheKey, watchedKeys);
      logger.debug("added cache for key: {}", cacheKey);
    } else {
      Cat.logEvent("ConfigFile-Cache-Hit", cacheKey);
    }

    return new ResponseEntity<>(result, responseHeaders,
        HttpStatus.OK);
  }

  String assembleCacheKey(String appId, String clusterName, String namespace,
                                  String dataCenter) {
    List<String> keyParts = Lists.newArrayList(appId, clusterName, namespace);
    if (!Strings.isNullOrEmpty(dataCenter)) {
      keyParts.add(dataCenter);
    }
    return STRING_JOINER.join(keyParts);
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    logger.info("message received - channel: {}, message: {}", channel, message);

    String content = message.getMessage();
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(content)) {
      return;
    }

    if (!watchedKeys2CacheKey.containsKey(content)) {
      return;
    }

    //create a new list to avoid ConcurrentModificationException
    List<String> cacheKeys = new ArrayList<>(watchedKeys2CacheKey.get(content));

    for (String cacheKey : cacheKeys) {
      logger.debug("invalidate cache key: {}", cacheKey);
      localCache.invalidate(cacheKey);
    }
  }
}
