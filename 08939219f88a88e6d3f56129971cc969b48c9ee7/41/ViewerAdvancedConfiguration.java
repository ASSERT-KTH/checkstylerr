package com.databasepreservation.common.client.models.configuration.collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"fixed", "picker"})
public class ViewerAdvancedConfiguration implements Serializable {

  private boolean fixed;
  private String picker;

  public ViewerAdvancedConfiguration() { }

  public boolean isFixed() {
    return fixed;
  }

  public void setFixed(boolean fixed) {
    this.fixed = fixed;
  }

  public String getPicker() {
    return picker;
  }

  public void setPicker(String picker) {
    this.picker = picker;
  }
}
