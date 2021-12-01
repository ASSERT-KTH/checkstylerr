package com.ctrip.apollo.core.dto;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigNotification {
  private String namespace;

  //for json converter
  public ApolloConfigNotification() {
  }

  public ApolloConfigNotification(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
}
