package com.ctrip.apollo.core.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * storage cud result
 */
public class ItemChangeSets {

  private String modifyBy;
  private List<ItemDTO> createItems;
  private List<ItemDTO> updateItems;
  private List<ItemDTO> deletedItems;

  public void addCreatedItem(ItemDTO item) {
    if (createItems == null) {
      createItems = new LinkedList<>();
    }
    createItems.add(item);
  }

  public void addupdateItem(ItemDTO item) {
    if (updateItems == null) {
      updateItems = new LinkedList<>();
    }
    updateItems.add(item);
  }

  public void addDeletedItem(ItemDTO item) {
    if (deletedItems == null) {
      deletedItems = new LinkedList<>();
    }
    deletedItems.add(item);
  }


  public List<ItemDTO> getCreateItems() {
    return createItems;
  }

  public List<ItemDTO> getUpdateItems() {
    return updateItems;
  }

  public List<ItemDTO> getDeletedItems() {
    return deletedItems;
  }

  public void setCreateItems(List<ItemDTO> createItems) {
    this.createItems = createItems;
  }

  public void setUpdateItems(List<ItemDTO> updateItems) {
    this.updateItems = updateItems;
  }

  public void setDeletedItems(List<ItemDTO> deletedItems) {
    this.deletedItems = deletedItems;
  }

  public String getModifyBy() {
    return modifyBy;
  }

  public void setModifyBy(String modifyBy) {
    this.modifyBy = modifyBy;
  }

}
