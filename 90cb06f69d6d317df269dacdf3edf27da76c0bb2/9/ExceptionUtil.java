package com.ctrip.apollo.util;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ExceptionUtil {
  public static String getDetailMessage(Throwable ex) {
    if (ex == null) {
      return "";
    }
    if (ex.getCause() != null) {
      return String.format("%s [Cause: %s]", ex.getMessage(), getDetailMessage(ex.getCause()));
    }
    return ex.getMessage();
  }
}
