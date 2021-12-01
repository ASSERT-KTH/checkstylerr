package com.ctrip.framework.apollo.portal.entity.vo;
import com.ctrip.framework.apollo.core.dto.ItemDTO;
import com.ctrip.framework.apollo.core.dto.NamespaceDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

import java.util.List;

public class NamespaceVO {
  private NamespaceDTO namespace;
  private int itemModifiedCnt;
  private List<ItemVO> items;
  private String format;
  private boolean isPublic;
  private String parentAppId;


  public NamespaceDTO getNamespace() {
    return namespace;
  }

  public void setNamespace(NamespaceDTO namespace) {
    this.namespace = namespace;
  }

  public int getItemModifiedCnt() {
    return itemModifiedCnt;
  }

  public void setItemModifiedCnt(int itemModifiedCnt) {
    this.itemModifiedCnt = itemModifiedCnt;
  }

  public List<ItemVO> getItems() {
    return items;
  }

  public void setItems(List<ItemVO> items) {
    this.items = items;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean aPublic) {
    isPublic = aPublic;
  }

  public String getParentAppId() {
    return parentAppId;
  }

  public void setParentAppId(String parentAppId) {
    this.parentAppId = parentAppId;
  }

  public static class ItemVO{
    private ItemDTO item;
    private boolean isModified;
    private String oldValue;
    private String newValue;

    public ItemDTO getItem() {
      return item;
    }

    public void setItem(ItemDTO item) {
      this.item = item;
    }

    public boolean isModified() {
      return isModified;
    }

    public void setModified(boolean isModified) {
      this.isModified = isModified;
    }

    public String getOldValue() {
      return oldValue;
    }

    public void setOldValue(String oldValue) {
      this.oldValue = oldValue;
    }

    public String getNewValue() {
      return newValue;
    }

    public void setNewValue(String newValue) {
      this.newValue = newValue;
    }


  }

}
