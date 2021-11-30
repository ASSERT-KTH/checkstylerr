/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.model.parameters;

import java.util.Collections;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Parameters {
  private final List<Parameter> parameters;
  private final List<ParameterGroup> groups;

  /**
   * Used to store a list of parameters and parameter groups
   *
   * @param parameters
   *          the (nullable) parameter list
   * @param groups
   *          the (nullable) parameter group list
   */
  public Parameters(List<Parameter> parameters, List<ParameterGroup> groups) {
    if (parameters != null) {
      this.parameters = parameters;
    } else {
      this.parameters = Collections.emptyList();
    }

    if (groups != null) {
      this.groups = groups;
    } else {
      this.groups = Collections.emptyList();
    }
  }

  public List<ParameterGroup> getGroups() {
    return groups;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }
}
