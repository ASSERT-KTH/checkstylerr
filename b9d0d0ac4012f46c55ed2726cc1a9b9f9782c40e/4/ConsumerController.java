package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.openapi.entity.Consumer;
import com.ctrip.framework.apollo.openapi.entity.ConsumerRole;
import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import com.ctrip.framework.apollo.openapi.service.ConsumerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
public class ConsumerController {

  private static final Date DEFAULT_EXPIRES = new GregorianCalendar(2099, Calendar.JANUARY, 1).getTime();

  @Autowired
  private ConsumerService consumerService;


  @Transactional
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @RequestMapping(value = "/consumers", method = RequestMethod.POST)
  public ConsumerToken createConsumer(@RequestBody Consumer consumer,
                                      @RequestParam(value = "expires", required = false)
                                      @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date
                                          expires) {

    if (StringUtils.isContainEmpty(consumer.getAppId(), consumer.getName(),
                                   consumer.getOwnerName(), consumer.getOrgId())) {
      throw new BadRequestException("Params(appId、name、ownerName、orgId) can not be empty.");
    }

    Consumer createdConsumer = consumerService.createConsumer(consumer);

    if (expires == null) {
      expires = DEFAULT_EXPIRES;
    }

    return consumerService.generateAndSaveConsumerToken(createdConsumer, expires);
  }

  @RequestMapping(value = "/consumers/by-appId", method = RequestMethod.GET)
  public ConsumerToken getConsumerTokenByAppId(@RequestParam String appId) {
    return consumerService.getConsumerTokenByAppId(appId);
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @RequestMapping(value = "/consumers/{token}/assign-role", method = RequestMethod.POST)
  public List<ConsumerRole> assignRoleToConsumer(@PathVariable String token, @RequestBody NamespaceDTO namespace) {

    String appId = namespace.getAppId();
    String namespaceName = namespace.getNamespaceName();

    if (StringUtils.isContainEmpty(appId, namespaceName)) {
      throw new BadRequestException("Params(AppId、NamespaceName) can not be empty.");
    }

    return consumerService.assignNamespaceRoleToConsumer(token, appId, namespaceName);
  }


}
