package com.ctrip.apollo.metaservice.controller;

import com.ctrip.apollo.core.dto.ServiceDTO;
import com.ctrip.apollo.metaservice.service.DiscoveryService;
import com.netflix.appinfo.InstanceInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
public class ServiceController {

  @Autowired
  private DiscoveryService discoveryService;


  @RequestMapping("/meta")
  public List<ServiceDTO> getMetaService() {
    List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
    List<ServiceDTO> result = instances.stream().map(new Function<InstanceInfo, ServiceDTO>() {

      @Override
      public ServiceDTO apply(InstanceInfo instance) {
        ServiceDTO service = new ServiceDTO();
        service.setAppName(instance.getAppName());
        service.setInstanceId(instance.getInstanceId());
        service.setHomepageUrl(instance.getHomePageUrl());
        return service;
      }

    }).collect(Collectors.toList());
    return result;
  }

  @RequestMapping("/config")
  public List<ServiceDTO> getConfigService() {
    List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
    List<ServiceDTO> result = instances.stream().map(new Function<InstanceInfo, ServiceDTO>() {

      @Override
      public ServiceDTO apply(InstanceInfo instance) {
        ServiceDTO service = new ServiceDTO();
        service.setAppName(instance.getAppName());
        service.setInstanceId(instance.getInstanceId());
        service.setHomepageUrl(instance.getHomePageUrl());
        return service;
      }

    }).collect(Collectors.toList());
    return result;
  }

  @RequestMapping("/admin")
  public List<ServiceDTO> getAdminService() {
    List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
    List<ServiceDTO> result = instances.stream().map(new Function<InstanceInfo, ServiceDTO>() {

      @Override
      public ServiceDTO apply(InstanceInfo instance) {
        ServiceDTO service = new ServiceDTO();
        service.setAppName(instance.getAppName());
        service.setInstanceId(instance.getInstanceId());
        service.setHomepageUrl(instance.getHomePageUrl());
        return service;
      }

    }).collect(Collectors.toList());
    return result;
  }
}
