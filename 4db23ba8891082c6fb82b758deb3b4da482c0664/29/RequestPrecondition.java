package com.ctrip.framework.apollo.portal.util;


import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.sun.istack.internal.Nullable;

public class RequestPrecondition {

  private static String CONTAIN_EMPTY_ARGUMENT = "request payload should not be contain empty.";

  private static String ILLEGAL_MODEL = "request model is invalid";

  public static void checkArgument(String... args) {
    checkArgument(!StringUtils.isContainEmpty(args), CONTAIN_EMPTY_ARGUMENT);
  }

  public static void checkModel(boolean valid){
    checkArgument(valid, ILLEGAL_MODEL);
  }

  public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
    if (!expression) {
      throw new BadRequestException(String.valueOf(errorMessage));
    }
  }



}
