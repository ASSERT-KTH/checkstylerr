package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.openapi.entity.ConsumerToken;
import com.ctrip.framework.apollo.openapi.service.ConsumerService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/consumers")
public class ConsumerController {
  private static final Date DEFAULT_EXPIRES = new GregorianCalendar(2099, Calendar.JANUARY, 1)
      .getTime();

  @Autowired
  private ConsumerService consumerService;
  @Autowired
  private UserInfoHolder userInfoHolder;

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @RequestMapping(value = "/{consumerId}/tokens", method = RequestMethod.POST)
  public ConsumerToken createConsumerToken(@PathVariable long consumerId,
                                           @RequestParam(value = "expires", required = false)
                                           @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date
                                               expires) {
    if (expires == null) {
      expires = DEFAULT_EXPIRES;
    }

    ConsumerToken consumerToken = generateConsumerToken(consumerId, expires);

    return consumerService.createConsumerToken(consumerToken);
  }

  private ConsumerToken generateConsumerToken(long consumerId, Date expires) {
    String createdBy = userInfoHolder.getUser().getUserId();
    Date createdTime = new Date();

    ConsumerToken consumerToken = new ConsumerToken();
    consumerToken.setConsumerId(consumerId);
    consumerToken.setExpires(expires);
    consumerToken.setDataChangeCreatedBy(createdBy);
    consumerToken.setDataChangeCreatedTime(createdTime);
    consumerToken.setDataChangeLastModifiedBy(createdBy);
    consumerToken.setDataChangeLastModifiedTime(createdTime);

    consumerService.generateAndEnrichConsumerToken(consumerToken);

    return consumerToken;
  }
}
