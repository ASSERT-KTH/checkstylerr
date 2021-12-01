package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkArgument;
import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

/**
 * 配置中心本身需要一些配置,这些配置放在数据库里面
 */
@RestController
public class ServerConfigController {

  @Autowired
  private ServerConfigRepository serverConfigRepository;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @RequestMapping(value = "/server/config", method = RequestMethod.POST)
  public ServerConfig createOrUpdate(@RequestBody ServerConfig serverConfig) {

    checkModel(serverConfig != null);
    checkArgument(serverConfig.getKey(), serverConfig.getValue());

    String modifiedBy = userInfoHolder.getUser().getUserId();

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
