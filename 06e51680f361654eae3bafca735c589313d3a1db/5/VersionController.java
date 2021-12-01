package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.service.VersionService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.VersionDTO;

@RestController
public class VersionController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private VersionService versionService;
  
  @RequestMapping("/app/{appId}/clusters/{clusterId}/versions")
  public List<VersionDTO> findVersions(@PathVariable("appId") String appId, @PathVariable("clusterId") Long clusterId) {
    List<Version> versions = viewService.findVersions(clusterId);
    return BeanUtils.batchTransform(VersionDTO.class, versions);
  }

  @RequestMapping("/versions/{versionId}")
  public VersionDTO findOne(@PathVariable("versionId") long versionId) {
    Version version = versionService.findOne(versionId);
    return BeanUtils.transfrom(VersionDTO.class, version);
  }
}
