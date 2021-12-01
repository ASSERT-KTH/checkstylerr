package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.service.NamespaceService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.NamespaceDTO;

@RestController
public class NamespaceController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces")
  public List<NamespaceDTO> findNamespaces(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    List<Namespace> groups = viewService.findNamespaces(appId, clusterName);
    return BeanUtils.batchTransform(NamespaceDTO.class, groups);
  }

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}")
  public NamespaceDTO getNamespace(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    Namespace namespace = namespaceService.findOne(appId,
        clusterName, namespaceName);
    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }

  @RequestMapping("/namespaces/{namespaceId}")
  public NamespaceDTO getNamespace(@PathVariable("namespaceId") Long namespaceId) {
    Namespace namespace = namespaceService.findOne(namespaceId);
    return BeanUtils.transfrom(NamespaceDTO.class, namespace);
  }
}
