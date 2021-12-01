package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.VersionRepository;

@Service
public class VersionService {

  @Autowired
  private VersionRepository versionRepository;
  
  public Version findOne(Long versionId){
    return versionRepository.findOne(versionId);
  }
}
