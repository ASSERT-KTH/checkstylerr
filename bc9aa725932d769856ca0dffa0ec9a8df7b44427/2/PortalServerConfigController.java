package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置中心本身需要一些配置,这些配置放在数据库里面
 */
@RestController
public class PortalServerConfigController {

  @Autowired
  private ServerConfigRepository serverConfigRepository;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @RequestMapping(value = "/server/config", method = RequestMethod.POST)
  public ServerConfig createOrUpdate(@RequestBody ServerConfig serverConfig) {

    if (serverConfig == null || StringUtils.isContainEmpty(serverConfig.getKey(), serverConfig.getValue())) {
      throw new BadRequestException("request payload contains empty");
    }

    String modifiedBy = userInfoHolder.getUser().getUsername();

    ServerConfig storedConfig = serverConfigRepository.findByKey(serverConfig.getKey());

    if (storedConfig == null) {//create
      serverConfig.setDataChangeCreatedBy(modifiedBy);
      serverConfig.setDataChangeLastModifiedBy(modifiedBy);
      return serverConfigRepository.save(serverConfig);
    } else {//update
      BeanUtils.copyEntityProperties(serverConfig, storedConfig);
      storedConfig.setDataChangeLastModifiedBy(modifiedBy);
      return serverConfigRepository.save(storedConfig);
    }


  }


}
