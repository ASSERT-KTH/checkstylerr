package com.ctrip.framework.apollo.portal.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.http.MultiResponseEntity;
import com.ctrip.framework.apollo.common.http.RichResponseEntity;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.PortalSettings;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.listener.AppCreationEvent;
import com.ctrip.framework.apollo.portal.service.AppService;

import java.util.List;

import static com.ctrip.framework.apollo.portal.util.RequestPrecondition.checkArgument;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;

  @Autowired
  private PortalSettings portalSettings;

  @Autowired
  private ApplicationEventPublisher publisher;

  @RequestMapping("")
  public List<App> findAllApp() {
    return appService.findAll();
  }

  @RequestMapping("/{appId}/navtree")
  public MultiResponseEntity<EnvClusterInfo> nav(@PathVariable String appId) {

    MultiResponseEntity<EnvClusterInfo> response = MultiResponseEntity.ok();
    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      try {
        response.addResponseEntity(RichResponseEntity.ok(appService.createEnvNavNode(env, appId)));
      } catch (Exception e) {
        response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                                            "load env:" + env.name() + " cluster error." + e
                                                                .getMessage()));
      }
    }
    return response;
  }

  /**
   * 创建App流程: 1.先在portal db中创建 2.再保存到各个环境的apollo db中
   *
   * 只要第一步成功,就算这次创建app是成功操作,如果某个环境的apollo db创建失败,可通过portal db中的app信息再次创建.
   */
  @RequestMapping(value = "", method = RequestMethod.POST)
  public ResponseEntity<Void> create(@RequestBody App app) {

    checkArgument(app.getName(), app.getAppId(), app.getOwnerEmail(), app.getOwnerName());

    appService.enrichUserInfo(app);
    App createdApp = appService.createOrUpdateAppInLocal(app);

    publisher.publishEvent(new AppCreationEvent(createdApp));

    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/envs/{env}", method = RequestMethod.POST, consumes = {"application/json"})
  public ResponseEntity<Void> create(@PathVariable String env, @RequestBody App app) {

    checkArgument(app.getName(), app.getAppId(), app.getOwnerEmail(), app.getOwnerName());

    appService.createApp(Env.valueOf(env), app);

    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/{appId}", method = RequestMethod.GET)
  public App load(@PathVariable String appId) {

    return appService.load(appId);
  }

  @RequestMapping(value = "/{appId}/miss_envs")
  public MultiResponseEntity<Env> findMissEnvs(@PathVariable String appId) {

    MultiResponseEntity<Env> response = MultiResponseEntity.ok();
    for (Env env : portalSettings.getActiveEnvs()) {
      try {
        appService.load(env, appId);
      } catch (Exception e) {
        if (e instanceof HttpClientErrorException &&
            ((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
          response.addResponseEntity(RichResponseEntity.ok(env));
        } else {
          response.addResponseEntity(RichResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR,
                                                              String.format("load appId:%s from env %s error.", appId, env)
                                                              + e.getMessage()));
        }
      }

    }

    return response;

  }

}
