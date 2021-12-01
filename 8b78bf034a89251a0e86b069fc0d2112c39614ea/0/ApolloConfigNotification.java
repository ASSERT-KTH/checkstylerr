package com.ctrip.framework.apollo.core.dto;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigNotification {
  private String namespaceName;
  private long notificationId;

  //for json converter
  public ApolloConfigNotification() {
  }

  public ApolloConfigNotification(String namespaceName, long notificationId) {
    this.namespaceName = namespaceName;
    this.notificationId = notificationId;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public long getNotificationId() {
    return notificationId;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  public void setNotificationId(long notificationId) {
    this.notificationId = notificationId;
  }

  @Override
  public String toString() {
    return "ApolloConfigNotification{" +
        "namespaceName='" + namespaceName + '\'' +
        ", notificationId=" + notificationId +
        '}';
  }
}
