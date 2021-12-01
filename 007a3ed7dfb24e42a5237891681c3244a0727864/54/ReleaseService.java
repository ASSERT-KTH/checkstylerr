package com.ctrip.framework.apollo.biz.service;

import com.google.gson.Gson;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;
import com.ctrip.framework.apollo.biz.utils.ReleaseKeyGenerator;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private ReleaseRepository releaseRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private AuditService auditService;

  private Gson gson = new Gson();

  public Release findOne(long releaseId) {
    Release release = releaseRepository.findOne(releaseId);
    return release;
  }

  public List<Release> findReleases(String appId, String clusterName, String namespaceName) {
    List<Release> releases = releaseRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
        clusterName, namespaceName);
    if (releases == null) {
      return Collections.emptyList();
    }
    return releases;
  }

  @Transactional
  public Release buildRelease(String name, String comment, Namespace namespace, String owner) {
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
    release.setDataChangeCreatedBy(owner);
    release.setDataChangeLastModifiedBy(owner);
    release.setName(name);
    release.setComment(comment);
    release.setAppId(namespace.getAppId());
    release.setClusterName(namespace.getClusterName());
    release.setNamespaceName(namespace.getNamespaceName());
    release.setConfigurations(gson.toJson(configurations));
    release = releaseRepository.save(release);

    auditService.audit(Release.class.getSimpleName(), release.getId(), Audit.OP.INSERT,
        release.getDataChangeCreatedBy());

    return release;
  }

}
