package com.ctrip.apollo.core.dto;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigNotification {
  private String namespaceName;

  //for json converter
  public ApolloConfigNotification() {
  }

  public ApolloConfigNotification(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }
}
