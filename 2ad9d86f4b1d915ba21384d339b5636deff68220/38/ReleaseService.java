package com.ctrip.framework.apollo.portal.service;

import com.google.gson.Gson;

import com.ctrip.framework.apollo.core.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.constant.CatEventType;
import com.ctrip.framework.apollo.portal.entity.form.NamespaceReleaseModel;
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
        releaseAPI.release(appId, env, clusterName, namespaceName, model.getReleaseTitle(), model.getReleaseComment()
            , userInfoHolder.getUser().getUserId());
    Cat.logEvent(CatEventType.RELEASE_NAMESPACE, String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));
    return releaseDTO;
  }

  public List<ReleaseVO> findReleases(String appId, Env env, String clusterName, String namespaceName, int page,
                                      int size) {
    List<ReleaseDTO> releaseDTOs = releaseAPI.findReleases(appId, env, clusterName, namespaceName, page, size);

    if (CollectionUtils.isEmpty(releaseDTOs)) {
      return Collections.EMPTY_LIST;
    }

    List<ReleaseVO> releases = new LinkedList<>();
    for (ReleaseDTO releaseDTO : releaseDTOs) {
      ReleaseVO release = new ReleaseVO();
      release.setBaseInfo(releaseDTO);

      Set<ReleaseVO.KVEntity> kvEntities = new LinkedHashSet<>();
      Set<Map.Entry> entries = gson.fromJson(releaseDTO.getConfigurations(), Map.class).entrySet();
      for (Map.Entry<String, String> entry : entries) {
        kvEntities.add(new ReleaseVO.KVEntity(entry.getKey(), entry.getValue()));
      }
      release.setItems(kvEntities);
      //为了减少数据量
      releaseDTO.setConfigurations("");
      releases.add(release);
    }

    return releases;
  }
}
