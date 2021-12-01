package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.biz.service.ReleaseService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.utils.StringUtils;

@RestController
public class ReleaseController {

  @Autowired
  private ViewService viewSerivce;

  @Autowired
  private ReleaseService releaseService;

  @Autowired
  private ConfigService configService;

  @RequestMapping("/release/{releaseId}")
  public ReleaseDTO findOne(@PathVariable("releaseId") long releaseId) {
    Release release = releaseService.findOne(releaseId);
    return BeanUtils.transfrom(ReleaseDTO.class, release);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
  public List<ReleaseDTO> findReleases(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    List<Release> releases = viewSerivce.findReleases(appId, clusterName, namespaceName);
    return BeanUtils.batchTransform(ReleaseDTO.class, releases);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest")
  public ReleaseDTO findLatestRelease(@PathVariable("appId") String appId,
                                      @PathVariable("clusterName") String clusterName,
                                      @PathVariable("namespaceName") String namespaceName){

    if (StringUtils.isContainEmpty(appId, clusterName, namespaceName)){
      return null;
    }
    Release release = configService.findRelease(appId, clusterName, namespaceName);
    return BeanUtils.transfrom(ReleaseDTO.class, release);
  }
}
