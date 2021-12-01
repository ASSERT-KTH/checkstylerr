package com.ctrip.apollo.common.utils;

import java.util.Map;

import org.springframework.web.client.HttpStatusCodeException;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;

public final class ExceptionUtils {

  private static Gson gson = new Gson();

  public static String toString(HttpStatusCodeException e) {
    @SuppressWarnings("unchecked")
    Map<String, Object> errorAttributes = gson.fromJson(e.getResponseBodyAsString(), Map.class);
    if (errorAttributes != null) {
      return MoreObjects.toStringHelper(HttpStatusCodeException.class)
          .add("status", errorAttributes.get("status"))
          .add("message", errorAttributes.get("message"))
          .add("timestamp", errorAttributes.get("timestamp"))
          .add("exception", errorAttributes.get("exception"))
          .add("errorCode", errorAttributes.get("errorCode"))
          .add("stackTrace", errorAttributes.get("stackTrace")).toString();
    }
    return "";
  }
}
