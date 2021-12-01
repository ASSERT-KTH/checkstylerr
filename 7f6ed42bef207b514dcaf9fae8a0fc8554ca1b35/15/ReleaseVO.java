package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.core.dto.ReleaseDTO;

import java.util.Set;

public class ReleaseVO {

  private ReleaseDTO baseInfo;

  private Set<KVEntity> items;

  public ReleaseDTO getBaseInfo() {
    return baseInfo;
  }

  public void setBaseInfo(ReleaseDTO baseInfo) {
    this.baseInfo = baseInfo;
  }

  public Set<KVEntity> getItems() {
    return items;
  }

  public void setItems(Set<KVEntity> items) {
    this.items = items;
  }

  public static class KVEntity{
    String key;
    String value;

    public KVEntity(String key, String value){
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
