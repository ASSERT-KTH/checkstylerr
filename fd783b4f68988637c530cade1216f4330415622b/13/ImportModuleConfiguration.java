package com.databasepreservation.model.modules.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"module", "parameters"})
public class ImportModuleConfiguration {

  private String moduleName;
  private Map<String, String> parameters;

  public ImportModuleConfiguration() {
    moduleName = "";
    parameters = new LinkedHashMap<>();
  }

  @JsonProperty("module")
  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  @JsonProperty("parameters")
  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }
}
