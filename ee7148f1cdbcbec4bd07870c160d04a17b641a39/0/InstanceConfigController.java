package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.InstanceService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.common.dto.InstanceConfigDTO;
import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping(path = "/instances")
public class InstanceConfigController {
  @Autowired
  private ReleaseService releaseService;
  @Autowired
  private InstanceService instanceService;

  @RequestMapping(value = "/by-release", method = RequestMethod.GET)
  public List<InstanceDTO> getByRelease(@RequestParam("releaseId") long releaseId,
                                        @RequestParam(value = "withReleaseDetail", defaultValue =
                                            "false") boolean withReleaseDetail,
                                        Pageable pageable) {
    Release release = releaseService.findOne(releaseId);
    if (release == null) {
      throw new NotFoundException(String.format("release not found for %s", releaseId));
    }
    List<InstanceConfig> instanceConfigs = instanceService.findActiveInstanceConfigsByReleaseKey
        (release.getReleaseKey(), pageable);

    if (instanceConfigs.isEmpty()) {
      return Collections.emptyList();
    }

    Map<Long, List<InstanceConfig>> instanceConfigMap = instanceConfigs.stream().collect(Collectors
        .groupingBy(InstanceConfig::getInstanceId));

    List<Instance> instances = instanceService.findInstancesByIds(instanceConfigMap.keySet());

    if (instances.isEmpty()) {
      return Collections.emptyList();
    }

    return instances.stream().map(transformToInstanceConfigDto).peek(instanceDTO -> {
      List<InstanceConfig> instanceConfigList = instanceConfigMap.get(instanceDTO.getId());
      ReleaseDTO releaseDTO = withReleaseDetail ? BeanUtils.transfrom(ReleaseDTO.class, release)
          : null;
      instanceDTO.setConfigs(instanceConfigList.stream()
          .map(instanceConfig -> transformToInstanceConfigDto(instanceConfig, releaseDTO))
          .collect(Collectors.toList()));
    }).collect(Collectors.toList());
  }

  private InstanceConfigDTO transformToInstanceConfigDto(InstanceConfig instanceConfig,
                                                         ReleaseDTO releaseDTO) {
    InstanceConfigDTO instanceConfigDTO = new InstanceConfigDTO();
    instanceConfigDTO.setDataChangeLastModifiedTime(instanceConfig
        .getDataChangeLastModifiedTime());
    instanceConfigDTO.setRelease(releaseDTO);
    return instanceConfigDTO;
  }

  private static Function<Instance, InstanceDTO> transformToInstanceConfigDto = instance -> {
    InstanceDTO instanceDTO = new InstanceDTO();
    instanceDTO.setId(instance.getId());
    instanceDTO.setAppId(instance.getAppId());
    instanceDTO.setClusterName(instance.getClusterName());
    instanceDTO.setDataCenter(instance.getDataCenter());
    instanceDTO.setIp(instance.getIp());
    instanceDTO.setDataChangeCreatedTime(instance.getDataChangeCreatedTime());

    return instanceDTO;
  };
}
