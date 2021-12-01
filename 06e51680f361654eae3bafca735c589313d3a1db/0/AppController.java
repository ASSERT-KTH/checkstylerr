package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.service.AppService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.AppDTO;

@RestController
public class AppController {

  @Autowired
  private AppService appService;
  
  @RequestMapping("/apps/{appId}")
  public AppDTO findByAppId(@PathVariable("appId") String appId) {
     App app = appService.findByAppId(appId);
     return BeanUtils.transfrom(AppDTO.class, app);
  }
  
  @RequestMapping("/apps")
  public List<AppDTO> findByName(@RequestParam("name") String name) {
     List<App> app = appService.findByName(name);
     return BeanUtils.batchTransform(AppDTO.class, app);
  }
}
