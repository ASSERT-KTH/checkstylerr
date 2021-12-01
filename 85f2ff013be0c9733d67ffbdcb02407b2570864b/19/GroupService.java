package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Group;
import com.ctrip.apollo.biz.repository.GroupRepository;

@Service
public class GroupService {

  @Autowired
  private GroupRepository groupRepository;
  
  public Group findOne(Long groupId){
    return groupRepository.findOne(groupId);
  }
}
