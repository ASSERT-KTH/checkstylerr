package com.ctrip.apollo.model;


import com.ctrip.apollo.enums.PropertyChangeType;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertyChange {
  private String propertyName;
  private Object oldValue;
  private Object newValue;
  private PropertyChangeType changeType;

  public PropertyChange(String propertyName, Object oldValue, Object newValue,
                        PropertyChangeType changeType) {
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.changeType = changeType;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public void setOldValue(Object oldValue) {
    this.oldValue = oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public void setNewValue(Object newValue) {
    this.newValue = newValue;
  }

  public PropertyChangeType getChangeType() {
    return changeType;
  }

  public void setChangeType(PropertyChangeType changeType) {
    this.changeType = changeType;
  }
}
