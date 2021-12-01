package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Validator {

  @Autowired
  private ServerConfigService serverConfigService;

  public boolean checkItemValueLength(String value){
    int lengthLimit = Integer.valueOf(serverConfigService.getValue("item.value.length.limit", "65536"));
    if (!StringUtils.isEmpty(value) && value.length() > lengthLimit){
      throw new BadRequestException("value too long. length limit:" + lengthLimit);
    }
    return true;
  }

}
