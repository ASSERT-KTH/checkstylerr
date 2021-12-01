package com.ctrip.framework.apollo.portal.service;

import com.google.gson.Gson;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseHistoryDTO;
import com.ctrip.framework.apollo.common.entity.EntityPair;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseHistoryVO;
import com.ctrip.framework.apollo.portal.util.RelativeDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReleaseHistoryService {

  private Gson gson = new Gson();


  @Autowired
  private AdminServiceAPI.ReleaseHistoryAPI releaseHistoryAPI;
  @Autowired
  private ReleaseService releaseService;


  public List<ReleaseHistoryVO> findNamespaceReleaseHistory(String appId, Env env, String clusterName,
                                                            String namespaceName, int page, int size) {
    PageDTO<ReleaseHistoryDTO> result = releaseHistoryAPI.findReleaseHistoriesByNamespace(appId, env, clusterName,
                                                                                          namespaceName, page, size);
    if (result == null || !result.hasContent()) {
      return Collections.emptyList();
    }

    List<ReleaseHistoryDTO> content = result.getContent();
    Set<Long> releaseIds = new HashSet<>();
    for (ReleaseHistoryDTO releaseHistoryDTO : content) {
      long releaseId = releaseHistoryDTO.getReleaseId();
      if (releaseId != 0) {
        releaseIds.add(releaseId);
      }
    }

    List<ReleaseDTO> releases = releaseService.findReleaseByIds(env, releaseIds);

    return convertReleaseHistoryDTO2VO(content, releases);
  }

  private List<ReleaseHistoryVO> convertReleaseHistoryDTO2VO(List<ReleaseHistoryDTO> source,
                                                             List<ReleaseDTO> releases) {

    Map<Long, ReleaseDTO> releasesMap = BeanUtils.mapByKey("id", releases);

    List<ReleaseHistoryVO> vos = new ArrayList<>(source.size());
    for (ReleaseHistoryDTO dto : source) {
      ReleaseHistoryVO vo = new ReleaseHistoryVO();
      vo.setId(dto.getId());
      vo.setAppId(dto.getAppId());
      vo.setClusterName(dto.getClusterName());
      vo.setNamespaceName(dto.getNamespaceName());
      vo.setBranchName(dto.getBranchName());
      vo.setReleaseId(dto.getReleaseId());
      vo.setPreviousReleaseId(dto.getPreviousReleaseId());
      vo.setOperator(dto.getDataChangeCreatedBy());
      vo.setOperation(dto.getOperation());
      Date releaseTime = dto.getDataChangeLastModifiedTime();
      vo.setReleaseTime(releaseTime);
      vo.setReleaseTimeFormatted(RelativeDateFormat.format(releaseTime));
      vo.setOperationContext(dto.getOperationContext());
      //set release info
      ReleaseDTO release = releasesMap.get(dto.getReleaseId());
      setReleaseInfoToReleaseHistoryVO(vo, release);

      vos.add(vo);
    }

    return vos;
  }

  private void setReleaseInfoToReleaseHistoryVO(ReleaseHistoryVO vo, ReleaseDTO release) {
    if (release != null) {
      vo.setReleaseTitle(release.getName());
      vo.setReleaseComment(release.getComment());

      Map<String, String> configuration = gson.fromJson(release.getConfigurations(), GsonType.CONFIG);
      List<EntityPair<String>> items = new ArrayList<>(configuration.size());
      for (Map.Entry<String, String> entry : configuration.entrySet()) {
        EntityPair<String> entityPair = new EntityPair<>(entry.getKey(), entry.getValue());
        items.add(entityPair);
      }
      vo.setConfiguration(items);

    } else {
      vo.setReleaseTitle("no release information");
      vo.setConfiguration(null);
    }
  }
}
