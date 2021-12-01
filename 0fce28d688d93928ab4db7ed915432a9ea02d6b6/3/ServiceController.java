package com.ctrip.apollo.metaservice.controller;

import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.metaservice.service.DiscoveryService;
import com.netflix.appinfo.InstanceInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

  @Autowired
  private DiscoveryService discoveryService;


  @RequestMapping("/meta")
  public List<ServiceDTO> getMetaService() {
    List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
    List<ServiceDTO> result = new ArrayList<ServiceDTO>();
    for (InstanceInfo instance : instances) {
      ServiceDTO service = new ServiceDTO();
      service.setAppName(instance.getAppName());
      service.setInstanceId(instance.getInstanceId());
      service.setHomepageUrl(instance.getHomePageUrl());
      result.add(service);
    }
    return result;
  }

  @RequestMapping("/config")
  public List<ServiceDTO> getConfigService() {
    List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
    List<ServiceDTO> result = new ArrayList<ServiceDTO>();
    for (InstanceInfo instance : instances) {
      ServiceDTO service = new ServiceDTO();
      service.setAppName(instance.getAppName());
      service.setInstanceId(instance.getInstanceId());
      service.setHomepageUrl(instance.getHomePageUrl());
      result.add(service);
    }
    return result;
  }

  @RequestMapping("/admin")
  public List<ServiceDTO> getAdminService() {
    List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
    List<ServiceDTO> result = new ArrayList<ServiceDTO>();
    for (InstanceInfo instance : instances) {
      ServiceDTO service = new ServiceDTO();
      service.setAppName(instance.getAppName());
      service.setInstanceId(instance.getInstanceId());
      service.setHomepageUrl(instance.getHomePageUrl());
      result.add(service);
    }
    return result;
  }
}
