package com.ctrip.framework.apollo.mockserver;

import com.ctrip.framework.apollo.core.MetaDomainConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create by zhangzheng on 8/22/18
 * Email:zhangzheng@youzan.com
 */
public class EmbeddedApollo extends ExternalResource {

  private Gson gson = new Gson();
  private Logger logger = LoggerFactory.getLogger(EmbeddedApollo.class);
  private Type notificationType = new TypeToken<List<ApolloConfigNotification>>(){}.getType();

  private String listenningUrl;
  private MockWebServer server;


  @Override
  protected void before() throws Throwable {
    server = new MockWebServer();
    final Dispatcher dispatcher = new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (request.getPath().startsWith("/services/config")) {

          return new MockResponse().setResponseCode(200)
              .setBody(mockConfigServiceAddr(listenningUrl));
        } else if (request.getPath().startsWith("/notifications/v2")) {
          String notifications = request.getRequestUrl().queryParameter("notifications");
          MockResponse response = new MockResponse().setResponseCode(200).setBody(mockLongPollBody(notifications));
          return response;
        } else if (request.getPath().startsWith("/configs")) {
          List<String> pathSegments = request.getRequestUrl().pathSegments();
          String appId = pathSegments.get(1);
          String cluster = pathSegments.get(2);
          String namespace = pathSegments.get(3);
          return new MockResponse().setResponseCode(200)
              .setBody(loadConfigFor(namespace));
        }
        return new MockResponse().setResponseCode(404);
      }
    };

    server.setDispatcher(dispatcher);
    server.start();
    //指定apollo的metaserver地址为localhost
    int port = server.getPort();
    this.listenningUrl = "http://localhost:"+port;

    MockedMetaServerProvider.setAddress(listenningUrl);

    System.setProperty("apollo.longPollingInitialDelayInMills","1");

    super.before();
  }

  @Override
  protected void after() {
    try {
      server.close();
    } catch (IOException e) {
      logger.error("stop apollo server error", e);
    }
  }


  private String loadConfigFor(String namespace){
    String filename = String.format("mockdata-%s.properties", namespace);
    final Properties prop = ResourceUtils.readConfigFile(filename, new Properties());
    Map<String,String> configurations = prop.stringPropertyNames().stream().collect(
        Collectors.toMap(key -> key, key -> prop.getProperty(key)));
    ApolloConfig apolloConfig = new ApolloConfig("someAppId", "someCluster",namespace,"someReleaseKey");

    Map<String,String> mergedConfigurations = mergeModifyByUser(namespace, configurations);
    apolloConfig.setConfigurations(mergedConfigurations);
    return gson.toJson(apolloConfig);
  }


  private String mockLongPollBody(String notificationsStr){
    List<ApolloConfigNotification> oldNotifications = gson.fromJson(notificationsStr, notificationType);
    List<ApolloConfigNotification> newNotifications = new ArrayList<>();
    for(ApolloConfigNotification noti: oldNotifications){
      newNotifications.add(new ApolloConfigNotification(noti.getNamespaceName(), noti.getNotificationId()+1));
    }
    return gson.toJson(newNotifications);
  }

  private String mockConfigServiceAddr(String addr){
    ServiceDTO serviceDTO = new ServiceDTO();
    serviceDTO.setAppName("someAppName");
    serviceDTO.setInstanceId("someInstanceId");
    serviceDTO.setHomepageUrl(addr);
    return gson.toJson(Arrays.asList(serviceDTO));
  }

  /**
   * 合并用户对namespace的修改
   * @param configurations
   * @return
   */
  private Map<String,String> mergeModifyByUser(String namespace
      , Map<String,String> configurations){
    if(addedPropertyOfNamespace.containsKey(namespace)){
      configurations.putAll(addedPropertyOfNamespace.get(namespace));
    }
    if(deletedKeysOfNamespace.containsKey(namespace)){
      for(String k: deletedKeysOfNamespace.get(namespace)){
        configurations.remove(k);
      }
    }
    return configurations;
  }


  private Map<String,Map<String,String>> addedPropertyOfNamespace = new HashMap<>();
  public void addOrModifyPropery(String namespace, String someKey, String someValue) {
    if(addedPropertyOfNamespace.containsKey(namespace)){
      addedPropertyOfNamespace.get(namespace).put(someKey, someValue);
    }else{
      addedPropertyOfNamespace.put(namespace, ImmutableMap.of(someKey, someValue));
    }
  }
  private Map<String,Set<String>> deletedKeysOfNamespace = new HashMap<>();
  public void delete(String namespace, String someKey) {
    if(deletedKeysOfNamespace.containsKey(namespace)){
      deletedKeysOfNamespace.get(namespace).add(someKey);
    }else{
      deletedKeysOfNamespace.put(namespace, ImmutableSet.of(someKey));
    }
  }


}
