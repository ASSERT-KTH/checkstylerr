package com.databasepreservation.common.client.models.status.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"id", "name", "customName", "description", "customDescription", "hide", "nestedColumn", "order", "template"})
public class ColumnStatus implements Serializable {

  private String id;
  private String name;
  private String customName;
  private String description;
  private String customDescription;
  private boolean hide;
  private boolean nestedColumn;
  private int order;
  private TemplateStatus templateStatus;

  public ColumnStatus() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isHide() {
    return hide;
  }

  public void setHide(boolean hide) {
    this.hide = hide;
  }

  public boolean isNestedColumn() {
    return nestedColumn;
  }

  public void setNestedColumn(boolean nestedColumn) {
    this.nestedColumn = nestedColumn;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @JsonProperty("template")
  public TemplateStatus getTemplateStatus() {
    return templateStatus;
  }

  public void setTemplateStatus(TemplateStatus templateStatus) {
    this.templateStatus = templateStatus;
  }
}
