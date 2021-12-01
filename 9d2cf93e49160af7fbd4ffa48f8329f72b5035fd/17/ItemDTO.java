package com.ctrip.apollo.core.dto;

public class ItemDTO {

  private long id;

  private long namespaceId;

  private String key;

  private String value;

  private String comment;

  public ItemDTO() {

  }

  public ItemDTO(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getComment() {
    return comment;
  }

  public long getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public long getNamespaceId() {
    return namespaceId;
  }

  public String getValue() {
    return value;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setNamespaceId(long namespaceId) {
    this.namespaceId = namespaceId;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
