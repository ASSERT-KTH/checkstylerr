package com.ctrip.apollo.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.core.exception.BadRequestException;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.entity.AppInfoVO;
import com.ctrip.apollo.portal.entity.ClusterNavTree;
import com.ctrip.apollo.portal.service.AppService;

import java.util.List;

@RestController
@RequestMapping("/apps")
public class AppController {

  @Autowired
  private AppService appService;


  @RequestMapping("/envs/{env}")
  public List<AppDTO> findAllApp(@PathVariable String env){
    if (StringUtils.isEmpty(env)){
      throw new BadRequestException("env can not be empty");
    }
    return appService.findAll(Env.valueOf(env));
  }

  @RequestMapping("/{appId}/navtree")
  public ClusterNavTree nav(@PathVariable String appId) {
    if (StringUtils.isEmpty(appId)) {
      throw new BadRequestException("app id can not be empty.");
    }

    return appService.buildClusterNavTree(appId);
  }

  @RequestMapping(value = "/envs/{env}", method = RequestMethod.POST, consumes = {"application/json"})
  public ResponseEntity<Void> create(@PathVariable String env, @RequestBody AppDTO app) {
    if (isInvalidApp(app)){
      throw new BadRequestException("request payload contains empty");
    }
    if ("ALL".equals(env)){
      appService.createAppInAllEnvs(app);
    } else {
      appService.createApp(Env.valueOf(env), app);
    }
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/{appId}", method = RequestMethod.GET)
  public AppInfoVO load(@PathVariable String appId){
    if (StringUtils.isEmpty(appId)){
      throw new BadRequestException("app id can not be empty.");
    }
    return appService.load(appId);
  }

  private boolean isInvalidApp(AppDTO app) {
    return StringUtils.isContainEmpty(app.getName(), app.getAppId(), app.getOwnerEmail(), app.getOwnerName());
  }
}

