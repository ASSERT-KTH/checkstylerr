package com.ctrip.framework.apollo.adminservice.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import com.ctrip.framework.apollo.biz.repository.NamespaceRepository;
import com.ctrip.framework.apollo.biz.repository.ReleaseHistoryRepository;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;
import com.ctrip.framework.apollo.biz.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.common.constants.ReleaseOperation;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseHistoryDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
public class ReleaseHistoryController {

  private Gson gson = new Gson();
  private Type configurationTypeReference = new TypeToken<Map<String, Object>>() {
  }.getType();

  @Autowired
  private ReleaseHistoryService releaseHistoryService;

  @Autowired
  private ReleaseRepository releaseRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private ReleaseHistoryRepository releaseHistoryRepository;

  @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories",
      method = RequestMethod.GET)
  public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(
      @PathVariable String appId, @PathVariable String clusterName,
      @PathVariable String namespaceName, Pageable pageable) {
    Page<ReleaseHistory> result = releaseHistoryService.findReleaseHistoriesByNamespace(appId,
        clusterName, namespaceName, pageable);

    if (!result.hasContent()) {
      return null;
    }

    List<ReleaseHistory> releaseHistories = result.getContent();
    List<ReleaseHistoryDTO> releaseHistoryDTOs = new ArrayList<>(releaseHistories.size());
    for (ReleaseHistory releaseHistory : releaseHistories) {
      ReleaseHistoryDTO dto = new ReleaseHistoryDTO();
      BeanUtils.copyProperties(releaseHistory, dto, "operationContext");
      dto.setOperationContext(gson.fromJson(releaseHistory.getOperationContext(),
          configurationTypeReference));

      releaseHistoryDTOs.add(dto);
    }


    return new PageDTO<>(releaseHistoryDTOs, pageable, result.getTotalElements());
  }

  @RequestMapping(value = "/release-histories/conversions", method = RequestMethod.POST)
  public void releaseHistoryConversion(
      @RequestParam(name = "namespaceId", required = false) String namespaceId) {
    Iterable<Namespace> namespaces;

    if (Strings.isNullOrEmpty(namespaceId)) {
      namespaces = namespaceRepository.findAll();
    } else {
      Set<Long> idList = Arrays.stream(namespaceId.split(",")).map(Long::valueOf).collect
          (Collectors.toSet());
      namespaces = namespaceRepository.findAll(idList);
    }

    for (Namespace namespace : namespaces) {
      List<Release> releases = releaseRepository
          .findByAppIdAndClusterNameAndNamespaceNameOrderByIdAsc(namespace.getAppId(), namespace
              .getClusterName(), namespace.getNamespaceName());
      if (CollectionUtils.isEmpty(releases)) {
        continue;
      }
      Release previousRelease = null;

      Set<ReleaseHistory> releaseHistories = Sets.newLinkedHashSet();//ordered set

      for (Release release : releases) {
        List<ReleaseHistory> histories = releaseHistoryService.findReleaseHistoriesByReleaseId
            (release.getId());
        //already processed
        if (!CollectionUtils.isEmpty(histories)) {
          continue;
        }

        long previousReleaseId = previousRelease == null ? 0 : previousRelease.getId();
        ReleaseHistory releaseHistory = assembleReleaseHistory(
            release, ReleaseOperation .NORMAL_RELEASE, previousReleaseId);
        releaseHistories.add(releaseHistory);

        //rollback
        if (release.isAbandoned() && previousRelease != null) {
          releaseHistory.setDataChangeLastModifiedTime(release.getDataChangeCreatedTime());
          ReleaseHistory rollBackReleaseHistory = assembleReleaseHistory(previousRelease,
              ReleaseOperation.ROLLBACK, release.getId());
          rollBackReleaseHistory.setDataChangeCreatedBy(release.getDataChangeLastModifiedBy());
          rollBackReleaseHistory.setDataChangeCreatedTime(release.getDataChangeLastModifiedTime());
          rollBackReleaseHistory.setDataChangeLastModifiedTime(release.getDataChangeLastModifiedTime());
          releaseHistories.add(rollBackReleaseHistory);
        } else {
          previousRelease = release;
        }
      }

      releaseHistoryRepository.save(releaseHistories);
    }
  }

  public ReleaseHistory assembleReleaseHistory(Release release, int releaseOperation, long
      previousReleaseId) {
    ReleaseHistory releaseHistory = new ReleaseHistory();
    releaseHistory.setAppId(release.getAppId());
    releaseHistory.setClusterName(release.getClusterName());
    releaseHistory.setNamespaceName(release.getNamespaceName());
    releaseHistory.setBranchName(release.getClusterName());
    releaseHistory.setReleaseId(release.getId());
    releaseHistory.setPreviousReleaseId(previousReleaseId);
    releaseHistory.setOperation(releaseOperation);
    releaseHistory.setOperationContext("{}"); //default empty object
    releaseHistory.setDataChangeCreatedBy(release.getDataChangeCreatedBy());
    releaseHistory.setDataChangeCreatedTime(release.getDataChangeCreatedTime());
    releaseHistory.setDataChangeLastModifiedTime(release.getDataChangeLastModifiedTime());
    releaseHistory.setDataChangeLastModifiedBy("apollo"); //mark

    return releaseHistory;
  }
}
