package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.VersionDTO;
import com.ctrip.apollo.portal.api.AdminServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class VersionService {

  @Autowired
  private AdminServiceAPI.VersionAPI versionAPI;

  public List<VersionDTO> findVersionsByApp(Apollo.Env env, long appId) {
    VersionDTO[] versions = versionAPI.getVersionsByApp(env, appId);

    if (versions == null || versions.length == 0){
      return Collections.EMPTY_LIST;
    }

    return Arrays.asList(versions);
  }
}
