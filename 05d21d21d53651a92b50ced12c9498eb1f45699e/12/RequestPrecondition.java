package com.ctrip.framework.apollo.common.utils;


import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.core.utils.StringUtils;


public class RequestPrecondition {

  private static String CONTAIN_EMPTY_ARGUMENT = "request payload should not be contain empty.";

  private static String ILLEGAL_MODEL = "request model is invalid";

  private static String ILLEGAL_NUMBER = "number should be positive";


  public static void checkArgument(String... args) {
    checkArgument(!StringUtils.isContainEmpty(args), CONTAIN_EMPTY_ARGUMENT);
  }

  public static void checkModel(boolean valid){
    checkArgument(valid, ILLEGAL_MODEL);
  }

  public static void checkArgument(boolean expression, Object errorMessage) {
    if (!expression) {
      throw new BadRequestException(String.valueOf(errorMessage));
    }
  }

  public static void checkNumberPositive(int... args){
    for (int num: args){
      if (num <= 0){
        throw new BadRequestException(ILLEGAL_NUMBER);
      }
    }
  }

  public static void checkNumberNotNegative(int... args){
    for (int num: args){
      if (num < 0){
        throw new BadRequestException(ILLEGAL_NUMBER);
      }
    }
  }



}
