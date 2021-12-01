package com.ctrip.apollo.enums;


/**
 * @author Jason Song(song_s@ctrip.com)
 */
public enum PropertyChangeType {
  NEW("New"),
  MODIFIED("Modified"),
  DELETED("Deleted");

  private String type;

  PropertyChangeType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
