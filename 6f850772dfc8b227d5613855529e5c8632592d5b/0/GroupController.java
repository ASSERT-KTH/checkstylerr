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
public class GroupController {

  @Autowired
  private ViewService viewService;

  @Autowired
  private NamespaceService groupService;

  @RequestMapping("/apps/{appId}/clusters/{clusterName}/groups")
  public List<NamespaceDTO> findGroups(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName) {
    List<Namespace> groups = viewService.findNamespaces(appId, clusterName);
    return BeanUtils.batchTransform(NamespaceDTO.class, groups);
  }

  @RequestMapping("/groups/{groupId}")
  public NamespaceDTO findOne(@PathVariable("groupId") Long groupId) {
    Namespace group = groupService.findOne(groupId);
    return BeanUtils.transfrom(NamespaceDTO.class, group);
  }
}
