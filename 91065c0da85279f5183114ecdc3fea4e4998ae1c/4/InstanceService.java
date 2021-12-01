package com.ctrip.framework.apollo.biz.service;

import com.google.common.base.Preconditions;

import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import com.ctrip.framework.apollo.biz.repository.InstanceConfigRepository;
import com.ctrip.framework.apollo.biz.repository.InstanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
