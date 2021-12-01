package com.ctrip.framework.apollo.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class InputValidator {
  public static final String INVALID_CLUSTER_NAMESPACE_MESSAGE = "只允许输入数字，字母和符号 - _ .";
  public static final String CLUSTER_NAMESPACE_VALIDATOR = "[0-9a-zA-z_.-]+";
  private static final Pattern CLUSTER_NAMESPACE_PATTERN =
      Pattern.compile(CLUSTER_NAMESPACE_VALIDATOR);

  public static boolean isValidClusterNamespace(String input) {
    Matcher matcher = CLUSTER_NAMESPACE_PATTERN.matcher(input);
    return matcher.matches();
  }
}
