/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.model.structure;

import java.util.List;

import com.databasepreservation.utils.ListUtils;

/**
 * @author Miguel Coutada
 */

public class RoutineStructure {

  private String name;

  private String description;

  private String source;

  private String body;

  private String characteristic;

  private String returnType;

  private List<Parameter> parameters;

  /**
         *
         */
  public RoutineStructure() {
  }

  /**
   * @param name
   * @param description
   * @param source
   * @param body
   * @param characteristic
   * @param returnType
   * @param parameters
   */
  public RoutineStructure(String name, String description, String source, String body, String characteristic,
    String returnType, List<Parameter> parameters) {
    this.name = name;
    this.description = description;
    this.source = source;
    this.body = body;
    this.characteristic = characteristic;
    this.returnType = returnType;
    this.parameters = parameters;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @param source
   *          the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the body
   */
  public String getBody() {
    return body;
  }

  /**
   * @param body
   *          the body to set
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * @return the characteristic
   */
  public String getCharacteristic() {
    return characteristic;
  }

  /**
   * @param characteristic
   *          the characteristic to set
   */
  public void setCharacteristic(String characteristic) {
    this.characteristic = characteristic;
  }

  /**
   * @return the returnType
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   * @param returnType
   *          the returnType to set
   */
  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  /**
   * @return the parameters
   */
  public List<Parameter> getParameters() {
    return parameters;
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RoutineStructure [name=");
    builder.append(name);
    builder.append(", description=");
    builder.append(description);
    builder.append(", source=");
    builder.append(source);
    builder.append(", body=");
    builder.append(body);
    builder.append(", characteristic=");
    builder.append(characteristic);
    builder.append(", returnType=");
    builder.append(returnType);
    builder.append(", parameters=");
    builder.append(parameters);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((characteristic == null) ? 0 : characteristic.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
    result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RoutineStructure other = (RoutineStructure) obj;
    if (body == null) {
      if (other.body != null) {
        return false;
      }
    } else if (!body.equals(other.body)) {
      return false;
    }
    if (characteristic == null) {
      if (other.characteristic != null) {
        return false;
      }
    } else if (!characteristic.equals(other.characteristic)) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!ListUtils.listEqualsWithoutOrder(parameters, other.parameters)) {
      return false;
    }
    if (returnType == null) {
      if (other.returnType != null) {
        return false;
      }
    } else if (!returnType.equals(other.returnType)) {
      return false;
    }
    if (source == null) {
      if (other.source != null) {
        return false;
      }
    } else if (!source.equals(other.source)) {
      return false;
    }
    return true;
  }
}
