package com.ctrip.framework.apollo.portal.entity.vo;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;

public class ItemDiffs {
  private NamespaceIdentifer namespace;
  private ItemChangeSets diffs;
  private String extInfo;

  public ItemDiffs(NamespaceIdentifer namespace){
    this.namespace = namespace;
  }
  public NamespaceIdentifer getNamespace() {
    return namespace;
  }

  public void setNamespace(NamespaceIdentifer namespace) {
    this.namespace = namespace;
  }

  public ItemChangeSets getDiffs() {
    return diffs;
  }

  public void setDiffs(ItemChangeSets diffs) {
    this.diffs = diffs;
  }

  public String getExtInfo() {
    return extInfo;
  }

  public void setExtInfo(String extInfo) {
    this.extInfo = extInfo;
  }
}
