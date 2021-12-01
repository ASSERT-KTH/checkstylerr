package com.ctrip.apollo.biz.service;

import com.google.common.base.Strings;

import com.ctrip.apollo.biz.entity.ReleaseSnapshot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ReleaseSnapShotRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
import com.ctrip.apollo.biz.utils.ApolloBeanUtils;
import com.ctrip.apollo.core.dto.ReleaseSnapshotDTO;
import com.ctrip.apollo.core.dto.VersionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service("adminReleaseService")
public class AdminReleaseService {
  @Autowired
  private ReleaseSnapShotRepository releaseSnapShotRepository;
  @Autowired
  private VersionRepository versionRepository;

  public List<ReleaseSnapshotDTO> findReleaseSnapshotByReleaseId(long releaseId) {
    if (releaseId <= 0) {
      return Collections.EMPTY_LIST;
    }

    List<ReleaseSnapshot> releaseSnapShots = releaseSnapShotRepository.findByReleaseId(releaseId);

    if (releaseSnapShots == null || releaseSnapShots.size() == 0) {
      return Collections.EMPTY_LIST;
    }

    return ApolloBeanUtils.batchTransform(ReleaseSnapshotDTO.class, releaseSnapShots);
  }

  public List<VersionDTO> findVersionsByApp(String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      return Collections.EMPTY_LIST;
    }

    List<Version> versions = versionRepository.findByAppId(appId);
    if (versions == null || versions.size() == 0) {
      return Collections.EMPTY_LIST;
    }

    return ApolloBeanUtils.batchTransform(VersionDTO.class, versions);
  }

  public VersionDTO loadVersionById(long versionId) {
    if (versionId <= 0) {
      return null;
    }
    Version version = versionRepository.findById(versionId);
    if (version == null) {
      return null;
    }
    VersionDTO dto = ApolloBeanUtils.transfrom(VersionDTO.class, version);
    return dto;
  }
}
