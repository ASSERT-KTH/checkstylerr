package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.message.MessageSender;
import com.ctrip.apollo.biz.message.Topics;
import com.ctrip.apollo.biz.service.ConfigService;
import com.ctrip.apollo.biz.service.NamespaceService;
import com.ctrip.apollo.biz.service.ReleaseService;
import com.ctrip.apollo.common.auth.ActiveUser;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.exception.NotFoundException;

@RestController
public class ReleaseController {

  @Autowired
  private ReleaseService releaseService;

  @Autowired
  private ConfigService configService;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private MessageSender messageSender;

  @RequestMapping("/release/{releaseId}")
  public ReleaseDTO get(@PathVariable("releaseId") long releaseId) {
    Release release = releaseService.findOne(releaseId);
    if (release == null)
      throw new NotFoundException(String.format("release not found for %s", releaseId));
    return BeanUtils.transfrom(ReleaseDTO.class, release);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
  public List<ReleaseDTO> find(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    List<Release> releases = releaseService.findReleases(appId, clusterName, namespaceName);
    return BeanUtils.batchTransform(ReleaseDTO.class, releases);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest")
  public ReleaseDTO getLatest(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    Release release = configService.findRelease(appId, clusterName, namespaceName);
    if (release == null) {
      throw new NotFoundException(String.format("latest release not found for %s %s %s", appId,
          clusterName, namespaceName));
    } else {
      return BeanUtils.transfrom(ReleaseDTO.class, release);
    }
  }

  @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", method = RequestMethod.POST)
  public ReleaseDTO buildRelease(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @RequestParam("name") String name,
      @RequestParam(name = "comment", required = false) String comment,
      @ActiveUser UserDetails user) {
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
          clusterName, namespaceName));
    }
    Release release = releaseService.buildRelease(name, comment, namespace, user.getUsername());
    messageSender.sendMessage(assembleKey(appId, clusterName, namespaceName),
        Topics.APOLLO_RELEASE_TOPIC);
    return BeanUtils.transfrom(ReleaseDTO.class, release);
  }

  private String assembleKey(String appId, String cluster, String namespace) {
    return String.format("%s-%s-%s", appId, cluster, namespace);
  }
}
