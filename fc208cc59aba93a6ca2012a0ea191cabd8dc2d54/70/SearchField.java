/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.main.common.shared.client.common.search;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchField implements Serializable {
  private static final long serialVersionUID = -2809811191632936028L;
  private String id;
  private List<String> searchFields;
  private String label;
  private String type;
  private boolean fixed;

  public SearchField() {
    super();
  }

  public SearchField(String id, List<String> searchFields, String label, String type) {
    super();
    this.id = id;
    this.searchFields = searchFields;
    this.label = label;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getSearchFields() {
    return searchFields;
  }

  public void setSearchFields(List<String> searchFields) {
    this.searchFields = searchFields;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isFixed() {
    return fixed;
  }

  public void setFixed(boolean fixed) {
    this.fixed = fixed;
  }

  @Override
  public String toString() {
    return "SearchField [id=" + id + ", searchFields=" + searchFields + ", label=" + label + ", type=" + type
      + ", fixed=" + fixed + "]";
  }
}
