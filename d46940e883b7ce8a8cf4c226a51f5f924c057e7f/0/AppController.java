package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.service.AppService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.exception.NotFoundException;
import com.ctrip.apollo.core.utils.StringUtils;

@RestController
public class AppController {

  @Autowired
  private AppService appService;

  @RequestMapping(path = "/apps/", method = RequestMethod.POST)
  public ResponseEntity<AppDTO> createApp(@RequestBody AppDTO appDTO) {
    App app = BeanUtils.transfrom(App.class, appDTO);
    app = appService.save(app);
    AppDTO dto = BeanUtils.transfrom(AppDTO.class, app);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @RequestMapping(path = "/apps/{appId}", method = RequestMethod.DELETE)
  public void deleteApp(@PathVariable("appId") String appId) {
    App app = appService.findOne(appId);
    if (app == null) throw new NotFoundException("app not found for appId " + appId);
    appService.delete(app.getId());
  }

  @RequestMapping("/apps")
  public List<AppDTO> findApps(@RequestParam(value = "name", required = false) String name,
      Pageable pageable) {
    List<App> app = null;
    if (StringUtils.isBlank(name)) {
      app = appService.findAll(pageable);
    } else {
      app = appService.findByName(name);
    }
    return BeanUtils.batchTransform(AppDTO.class, app);
  }

  @RequestMapping("/apps/{appId}")
  public AppDTO getApp(@PathVariable("appId") String appId) {
    App app = appService.findOne(appId);
    if (app == null) throw new NotFoundException("app not found for appId " + appId);
    return BeanUtils.transfrom(AppDTO.class, app);
  }

  @RequestMapping(path = "/apps/{appId}", method = RequestMethod.PUT)
  public AppDTO updateApp(@PathVariable("appId") String appId, @RequestBody AppDTO appDTO) {
    if (!appId.equals(appDTO.getAppId())) {
      throw new IllegalArgumentException(String
          .format("Path variable %s is not equals to object field %s", appId, appDTO.getAppId()));
    }
    App app = appService.findOne(appId);
    if (app == null) throw new NotFoundException("app not found for appId " + appId);
    app = appService.update(BeanUtils.transfrom(App.class, appDTO));
    return BeanUtils.transfrom(AppDTO.class, app);
  }

}
