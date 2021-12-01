package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Commit;
import com.ctrip.framework.apollo.biz.repository.CommitRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CommitService {

  @Autowired
  private CommitRepository commitRepository;

  public void save(Commit commit, String user){

    commit.setId(0);//protection
    commit.setDataChangeCreatedBy(user);
    commit.setDataChangeCreatedTime(new Date());
    commitRepository.save(commit);
  }

}
