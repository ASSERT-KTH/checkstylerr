package com.ctrip.apollo.model;


import com.google.common.base.MoreObjects;

import com.ctrip.apollo.enums.PropertyChangeType;

/**
 * Holds the information for a config change.
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigChange {
  private final String propertyName;
  private String oldValue;
  private String newValue;
  private PropertyChangeType changeType;

  /**
   * Constructor.
   * @param propertyName the key whose value is changed
   * @param oldValue the value before change
   * @param newValue the value after change
   * @param changeType the change type
   */
  public ConfigChange(String propertyName, String oldValue, String newValue,
                      PropertyChangeType changeType) {
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.changeType = changeType;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getOldValue() {
    return oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public PropertyChangeType getChangeType() {
    return changeType;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public void setChangeType(PropertyChangeType changeType) {
    this.changeType = changeType;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("propertyName", propertyName)
        .add("oldValue", oldValue)
        .add("newValue", newValue)
        .add("changeType", changeType)
        .toString();
  }
}
