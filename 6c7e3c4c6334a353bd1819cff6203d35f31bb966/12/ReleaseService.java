package com.ctrip.framework.apollo.biz.service;

import com.google.gson.Gson;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.NamespaceLock;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;
import com.ctrip.framework.apollo.biz.utils.ReleaseKeyGenerator;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseService {

  private Gson gson = new Gson();

  @Autowired
  private ReleaseRepository releaseRepository;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private AuditService auditService;
  @Autowired
  private NamespaceLockService namespaceLockService;

  public Release findOne(long releaseId) {
    Release release = releaseRepository.findOne(releaseId);
    return release;
  }

  public Release findLatestActiveRelease(String appId, String clusterName, String namespaceName) {
    Release release = releaseRepository.findFirstByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(
        appId, clusterName, namespaceName);
    return release;
  }

  public List<Release> findAllReleases(String appId, String clusterName, String namespaceName, Pageable page) {
    List<Release> releases = releaseRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(appId,
                                                                                                      clusterName,
                                                                                                      namespaceName,
                                                                                                      page);
    if (releases == null) {
      return Collections.emptyList();
    }
    return releases;
  }

  public List<Release> findActiveReleases(String appId, String clusterName, String namespaceName, Pageable page) {
    List<Release>
        releases =
        releaseRepository.findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(appId,
                                                                                                    clusterName,
                                                                                                    namespaceName,
                                                                                                    page);
    if (releases == null) {
      return Collections.emptyList();
    }
    return releases;
  }

  @Transactional
  public Release buildRelease(String name, String comment, Namespace namespace, String operator) {
    NamespaceLock lock = namespaceLockService.findLock(namespace.getId());
    if (lock != null && lock.getDataChangeCreatedBy().equals(operator)) {
      throw new BadRequestException(String.format("Current editor %s is not allowed to release the config", operator));
    }

    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespace.getId());
    Map<String, String> configurations = new HashMap<String, String>();
    for (Item item : items) {
      if (StringUtils.isEmpty(item.getKey())) {
        continue;
      }
      configurations.put(item.getKey(), item.getValue());
    }

    Release release = new Release();
    release.setReleaseKey(ReleaseKeyGenerator.generateReleaseKey(namespace));
    release.setDataChangeCreatedTime(new Date());
    release.setDataChangeCreatedBy(operator);
    release.setDataChangeLastModifiedBy(operator);
    release.setName(name);
    release.setComment(comment);
    release.setAppId(namespace.getAppId());
    release.setClusterName(namespace.getClusterName());
    release.setNamespaceName(namespace.getNamespaceName());
    release.setConfigurations(gson.toJson(configurations));
    release = releaseRepository.save(release);

    namespaceLockService.unlock(namespace.getId());
    auditService.audit(Release.class.getSimpleName(), release.getId(), Audit.OP.INSERT,
                       release.getDataChangeCreatedBy());

    return release;
  }

  @Transactional
  public Release rollback(long releaseId, String operator) {
    Release release = findOne(releaseId);
    if (release == null){
      throw new NotFoundException("release not found");
    }
    if (release.isAbandoned()){
      throw new BadRequestException("release is not active");
    }

    String appId = release.getAppId();
    String clusterName = release.getClusterName();
    String namespaceName = release.getNamespaceName();

    PageRequest page = new PageRequest(0, 2);
    List<Release> twoLatestActiveReleases = findActiveReleases(appId, clusterName, namespaceName, page);
    if (twoLatestActiveReleases == null || twoLatestActiveReleases.size() < 2) {
      throw new BadRequestException(String.format(
          "Can't rollback namespace(appId=%s, clusterName=%s, namespaceName=%s) because there is only one active release", appId,
          clusterName,
          namespaceName));
    }

    release.setAbandoned(true);
    release.setDataChangeLastModifiedBy(operator);

    return releaseRepository.save(release);
  }

  @Transactional
  public int batchDelete(String appId, String clusterName, String namespaceName, String operator){
    return releaseRepository.batchDelete(appId, clusterName, namespaceName, operator);
  }
}
