package com.ctrip.apollo.biz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.App;
import com.ctrip.apollo.biz.repository.AppRepository;

@Service
public class AppService {

  @Autowired
  private AppRepository appRepository;
  
  public App save(App entity){
    return appRepository.save(entity);
  }
  
  public List<App> findAll(Pageable pageable){
     Page<App> page = appRepository.findAll(pageable);
     return page.getContent();
  }
  
  public List<App> findByName(String name){
    return appRepository.findByName(name);
  }
  
  public App findOne(String appId){
    return appRepository.findByAppId(appId);
  }
}
