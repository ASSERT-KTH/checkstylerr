package com.ctrip.apollo.biz.service;

import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ConfigItemDTO;
import com.ctrip.apollo.core.dto.ReleaseSnapshotDTO;
import com.ctrip.apollo.core.dto.VersionDTO;

import java.util.List;

/**
 * config service for admin
 */
public interface AdminConfigService {

  List<ReleaseSnapshotDTO> findReleaseSnapshotByReleaseId(long releaseId);

  List<VersionDTO> findVersionsByApp(long appId);

  VersionDTO loadVersionById(long versionId);

  List<ClusterDTO> findClustersByApp(long appId);

  List<ConfigItemDTO> findConfigItemsByClusters(List<Long> clusterIds);

}
