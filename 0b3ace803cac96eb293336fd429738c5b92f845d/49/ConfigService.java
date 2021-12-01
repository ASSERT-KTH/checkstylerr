package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Config Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConfigService {

  @Autowired
  private ReleaseRepository releaseRepository;

  public Release findRelease(String appId, String clusterName, String namespaceName) {
    Release release = releaseRepository.findFirstByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(
        appId, clusterName, namespaceName);
    return release;
  }
}
