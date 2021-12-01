package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.Group;
import com.ctrip.apollo.biz.service.GroupService;
import com.ctrip.apollo.biz.service.ViewService;
import com.ctrip.apollo.biz.utils.BeanUtils;
import com.ctrip.apollo.core.dto.GroupDTO;

@RestController
public class GroupController {
  
  @Autowired
  private ViewService viewService;

  @Autowired
  private GroupService groupService;
  
  @RequestMapping("/apps/{appId}/clusters/{clusterId}/groups")
  public List<GroupDTO> findGroups(@PathVariable("clusterId") Long clusterId) {
    List<Group> groups = viewService.findGroups(clusterId);
    return BeanUtils.batchTransform(GroupDTO.class, groups);
  }
  
  @RequestMapping("/groups/{groupId}")
  public GroupDTO findOne(@PathVariable("groupId") Long groupId){
    Group group = groupService.findOne(groupId);
    return BeanUtils.transfrom(GroupDTO.class, group);
  }
}
