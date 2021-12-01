package com.ctrip.framework.apollo.portal.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.entity.vo.Organization;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationController {
  @Autowired
  private ServerConfigRepository serverConfigRepository;

  @Autowired
  private Gson gson;

  private Type responseType = new TypeToken<List<Organization>>() {
  }.getType();

  @RequestMapping
  public List<Organization> loadOrganization() {
    ServerConfig config = serverConfigRepository.findByKey("organizations");
    return gson.fromJson(config.getValue(), responseType);
  }
}
