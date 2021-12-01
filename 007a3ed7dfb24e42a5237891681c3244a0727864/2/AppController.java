package com.ctrip.framework.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.framework.apollo.biz.entity.App;
import com.ctrip.framework.apollo.biz.service.AdminService;
import com.ctrip.framework.apollo.biz.service.AppService;
import com.ctrip.framework.apollo.common.auth.ActiveUser;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.dto.AppDTO;
import com.ctrip.framework.apollo.core.exception.NotFoundException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

@RestController
public class AppController {

  @Autowired
  private AppService appService;

  @Autowired
  private AdminService adminService;

  @RequestMapping(path = "/apps", method = RequestMethod.POST)
  public AppDTO createOrUpdate(@RequestBody AppDTO dto, @ActiveUser UserDetails user) {
    App entity = BeanUtils.transfrom(App.class, dto);
    App managedEntity = appService.findOne(entity.getAppId());
    if (managedEntity != null) {
      managedEntity.setDataChangeLastModifiedBy(user.getUsername());
      BeanUtils.copyEntityProperties(entity, managedEntity);
      entity = appService.update(managedEntity);
    } else {
      entity.setDataChangeCreatedBy(user.getUsername());
      entity.setDataChangeLastModifiedBy(user.getUsername());
      entity = adminService.createNewApp(entity);
    }

    dto = BeanUtils.transfrom(AppDTO.class, entity);
    return dto;
  }

  @RequestMapping(path = "/apps/{appId}", method = RequestMethod.DELETE)
  public void delete(@PathVariable("appId") String appId, @ActiveUser UserDetails user) {
    App entity = appService.findOne(appId);
    if (entity == null) throw new NotFoundException("app not found for appId " + appId);
    appService.delete(entity.getId(), user.getUsername());
  }

  @RequestMapping("/apps")
  public List<AppDTO> find(@RequestParam(value = "name", required = false) String name,
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
  public AppDTO get(@PathVariable("appId") String appId) {
    App app = appService.findOne(appId);
    if (app == null) throw new NotFoundException("app not found for appId " + appId);
    return BeanUtils.transfrom(AppDTO.class, app);
  }

  @RequestMapping("/apps/{appId}/unique")
  public boolean isAppIdUnique(@PathVariable("appId") String appId) {
    return appService.isAppIdUnique(appId);
  }

}
