package com.ctrip.apollo.biz.converter;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.ConfigItem;
import com.ctrip.apollo.biz.entity.ReleaseSnapshot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ConfigItemDTO;
import com.ctrip.apollo.core.dto.ReleaseSnapshotDTO;
import com.ctrip.apollo.core.dto.VersionDTO;

public class EntityToDTOConverter {

  public static ClusterDTO convert(Cluster source) {
    ClusterDTO target = new ClusterDTO();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

  public static ConfigItemDTO convert(ConfigItem source) {
    ConfigItemDTO target = new ConfigItemDTO();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

  public static ReleaseSnapshotDTO convert(ReleaseSnapshot source) {
    ReleaseSnapshotDTO target = new ReleaseSnapshotDTO();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

  public static VersionDTO convert(Version source) {
    VersionDTO target = new VersionDTO();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

}
