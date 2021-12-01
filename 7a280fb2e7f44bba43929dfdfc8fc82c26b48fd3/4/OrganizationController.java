package com.ctrip.framework.apollo.portal.controller;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.portal.entity.vo.Organization;
import com.ctrip.framework.apollo.portal.service.ServerConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {
  @Autowired
  private ServerConfigService serverConfigService;

  @Autowired
  private Gson gson;

  private Type responseType = new TypeToken<List<Organization>>() {
  }.getType();

  @RequestMapping
  public List<Organization> loadOrganization() {
    String organizations = serverConfigService.getValue("organizations");
    if (Strings.isNullOrEmpty(organizations)) {
      return Collections.emptyList();
    }

    return gson.fromJson(organizations, responseType);
  }
}
