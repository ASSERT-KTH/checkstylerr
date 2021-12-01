/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumnsParameter implements Serializable {

  private String schemaName;
  private String name;
  private List<ColumnParameter> columns;

  public TableAndColumnsParameter() {
    columns = new ArrayList<>();
  }

  public TableAndColumnsParameter(String schemaName, String name, List<ColumnParameter> columns) {
    this.schemaName = schemaName;
    this.name = name;
    this.columns = columns;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ColumnParameter> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnParameter> columns) {
    this.columns = columns;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TableAndColumnsParameter parameter = (TableAndColumnsParameter) o;
    return Objects.equals(getSchemaName(), parameter.getSchemaName())
      && Objects.equals(getName(), parameter.getName())
      && Objects.equals(getColumns(), parameter.getColumns());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSchemaName(), getName(), getColumns());
  }
}
