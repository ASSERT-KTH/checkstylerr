package com.databasepreservation.model.modules.configuration;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "externalLOB"})
public class ColumnConfiguration {
  private String name;
  private ExternalLobsConfiguration externalLob;

  public ColumnConfiguration() {
    externalLob = null;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("externalLOB")
  public ExternalLobsConfiguration getExternalLob() {
    return externalLob;
  }

  public void setExternalLob(ExternalLobsConfiguration externalLob) {
    this.externalLob = externalLob;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ColumnConfiguration that = (ColumnConfiguration) o;
    return Objects.equals(getName(), that.getName()) && Objects.equals(getExternalLob(), that.getExternalLob());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getExternalLob());
  }
}
