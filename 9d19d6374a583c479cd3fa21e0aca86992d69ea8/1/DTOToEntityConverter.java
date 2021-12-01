package com.ctrip.apollo.biz.converter;

import org.springframework.beans.BeanUtils;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.ConfigItem;
import com.ctrip.apollo.biz.entity.ReleaseSnapshot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ConfigItemDTO;
import com.ctrip.apollo.core.dto.ReleaseSnapshotDTO;
import com.ctrip.apollo.core.dto.VersionDTO;

public class DTOToEntityConverter {

  public static Cluster convert(ClusterDTO source) {
    Cluster target = new Cluster();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

  public static ConfigItem convert(ConfigItemDTO source) {
    ConfigItem target = new ConfigItem();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

  public static ReleaseSnapshot convert(ReleaseSnapshotDTO source) {
    ReleaseSnapshot target = new ReleaseSnapshot();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

  public static Version convert(VersionDTO source) {
    Version target = new Version();
    BeanUtils.copyProperties(source, target, ConverterUtils.getNullPropertyNames(source));
    return target;
  }

}
