package com.ctrip.framework.apollo.portal.service;

import com.google.common.base.Objects;
import com.google.gson.Gson;

import com.ctrip.framework.apollo.core.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.constant.CatEventType;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.entity.vo.KVEntity;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseVO;
import com.dianping.cat.Cat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReleaseService {

  private static final Gson gson = new Gson();

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private AdminServiceAPI.ReleaseAPI releaseAPI;

  public ReleaseDTO createRelease(NamespaceReleaseModel model) {
    String appId = model.getAppId();
    Env env = model.getEnv();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();
    ReleaseDTO releaseDTO =
        releaseAPI
            .createRelease(appId, env, clusterName, namespaceName, model.getReleaseTitle(), model.getReleaseComment()
                , userInfoHolder.getUser().getUserId());
    Cat.logEvent(CatEventType.RELEASE_NAMESPACE, String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    return releaseDTO;
  }

  public List<ReleaseVO> findAllReleases(String appId, Env env, String clusterName, String namespaceName, int page,
                                         int size) {
    List<ReleaseDTO> releaseDTOs = releaseAPI.findAllReleases(appId, env, clusterName, namespaceName, page, size);

    if (CollectionUtils.isEmpty(releaseDTOs)) {
      return Collections.EMPTY_LIST;
    }

    List<ReleaseVO> releases = new LinkedList<>();
    for (ReleaseDTO releaseDTO : releaseDTOs) {
      ReleaseVO release = new ReleaseVO();
      release.setBaseInfo(releaseDTO);

      Set<KVEntity> kvEntities = new LinkedHashSet<>();
      Set<Map.Entry> entries = gson.fromJson(releaseDTO.getConfigurations(), Map.class).entrySet();
      for (Map.Entry<String, String> entry : entries) {
        kvEntities.add(new KVEntity(entry.getKey(), entry.getValue()));
      }
      release.setItems(kvEntities);
      //为了减少数据量
      releaseDTO.setConfigurations("");
      releases.add(release);
    }

    return releases;
  }

  public List<ReleaseDTO> findActiveReleases(String appId, Env env, String clusterName, String namespaceName, int page,
                                             int size) {
    return releaseAPI.findActiveReleases(appId, env, clusterName, namespaceName, page, size);
  }

  public void rollback(Env env, long releaseId) {
    releaseAPI.rollback(env, releaseId, userInfoHolder.getUser().getUserId());
  }

  public ReleaseCompareResult compare(Env env, long firstReleaseId, long secondReleaseId) {
    ReleaseDTO firstRelease = releaseAPI.loadRelease(env, firstReleaseId);
    ReleaseDTO secondRelease = releaseAPI.loadRelease(env, secondReleaseId);

    Map<String, String> firstItems = gson.fromJson(firstRelease.getConfigurations(), Map.class);
    Map<String, String> secondItems = gson.fromJson(secondRelease.getConfigurations(), Map.class);

    ReleaseCompareResult compareResult = new ReleaseCompareResult();

    //added and modified in firstRelease
    for (Map.Entry<String, String> entry : firstItems.entrySet()) {
      String key = entry.getKey();
      String firstValue = entry.getValue();
      String secondValue = secondItems.get(key);
      if (!Objects.equal(firstValue, secondValue)) {
        compareResult.addEntityPair(new KVEntity(key, firstValue), new KVEntity(key, secondValue));
      }
    }

    //deleted in firstRelease
    for (Map.Entry<String, String> entry : secondItems.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (firstItems.get(key) == null) {
        compareResult.addEntityPair(new KVEntity(key, ""), new KVEntity(key, value));
      }

    }

    return compareResult;
  }
}
