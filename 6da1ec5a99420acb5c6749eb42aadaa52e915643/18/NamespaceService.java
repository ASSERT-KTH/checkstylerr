package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.repository.NamespaceRepository;

@Service
public class NamespaceService {

  @Autowired
  private NamespaceRepository namespaceRepository;
  
  public Namespace findOne(Long namespaceId){
    return namespaceRepository.findOne(namespaceId);
  }
}
