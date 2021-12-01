package com.ctrip.framework.apollo.biz.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import com.ctrip.framework.apollo.biz.repository.InstanceConfigRepository;
import com.ctrip.framework.apollo.biz.repository.InstanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class InstanceService {
  @Autowired
  private InstanceRepository instanceRepository;

  @Autowired
  private InstanceConfigRepository instanceConfigRepository;

  public Instance findInstance(String appId, String clusterName, String dataCenter, String ip) {
    return instanceRepository.findByAppIdAndClusterNameAndDataCenterAndIp(appId, clusterName,
        dataCenter, ip);
  }

  public List<Instance> findInstancesByIds(Set<Long> instanceIds) {
    Iterable<Instance> instances = instanceRepository.findAll(instanceIds);
    if (instances == null) {
      return Collections.emptyList();
    }
    return Lists.newArrayList(instances);
  }

  @Transactional
  public Instance createInstance(Instance instance) {
    instance.setId(0); //protection

    return instanceRepository.save(instance);
  }

  public InstanceConfig findInstanceConfig(long instanceId, String configAppId,
                                           String configNamespaceName) {
    return instanceConfigRepository.findByInstanceIdAndConfigAppIdAndConfigNamespaceName(
        instanceId, configAppId, configNamespaceName);
  }

  public List<InstanceConfig> findActiveInstanceConfigsByReleaseKey(String releaseKey, Pageable
      pageable) {
    List<InstanceConfig> instanceConfigs = instanceConfigRepository
        .findByReleaseKeyAndDataChangeLastModifiedTimeAfter(releaseKey,
            getValidInstanceConfigDate(), pageable);
    if (instanceConfigs == null) {
      return Collections.emptyList();
    }
    return instanceConfigs;
  }

  /**
   * Currently the instance config is expired by 1 day, add one more hour to avoid possible time
   * difference
   */
  private Date getValidInstanceConfigDate() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    cal.add(Calendar.HOUR, -1);
    return cal.getTime();
  }

  @Transactional
  public InstanceConfig createInstanceConfig(InstanceConfig instanceConfig) {
    instanceConfig.setId(0); //protection

    return instanceConfigRepository.save(instanceConfig);
  }

  @Transactional
  public InstanceConfig updateInstanceConfig(InstanceConfig instanceConfig) {
    InstanceConfig existedInstanceConfig = instanceConfigRepository.findOne(instanceConfig.getId());
    Preconditions.checkArgument(existedInstanceConfig != null, String.format(
        "Instance config %d doesn't exist", instanceConfig.getId()));

    existedInstanceConfig.setReleaseKey(instanceConfig.getReleaseKey());
    existedInstanceConfig.setDataChangeLastModifiedTime(instanceConfig
        .getDataChangeLastModifiedTime());

    return instanceConfigRepository.save(existedInstanceConfig);
  }
}
