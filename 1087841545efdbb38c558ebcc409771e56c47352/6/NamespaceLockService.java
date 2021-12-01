package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.repository.NamespaceLockRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceLockService {

  @Autowired
  private NamespaceLockRepository namespaceLockRepository;


  public NamespaceLock findLock(Long namespaceId){
    return namespaceLockRepository.findByNamespaceId(namespaceId);
  }

  public NamespaceLock tryLock(NamespaceLock lock){
    return namespaceLockRepository.save(lock);
  }

  public void unlock(Long namespaceId){
    namespaceLockRepository.deleteByNamespaceId(namespaceId);
  }
}
