package com.ctrip.apollo.biz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.repository.ReleaseRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseService {
  
  @Autowired
  private ReleaseRepository releaseRepository;
  
  public Release findOne(long releaseId) {
    Release release = releaseRepository.findOne(releaseId);
    return release;
  }

}
