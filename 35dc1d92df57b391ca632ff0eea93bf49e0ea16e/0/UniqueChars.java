/*
 * Copyright (C) 2014 Pedro Vicente Gómez Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pedrovgs.problem50;

import java.util.HashMap;
import java.util.Map;

/**
 * Implement an algorithm to determine if a string has all unique characters. What if you cannot
 * use additional data structures?
 *
 * @author Pedro Vicente Gómez Sánchez.
 */
public class UniqueChars {

  /**
   * Iterative algorithm to solve this problem. The complexity order of this algorithm in space an
   * time terms is equals to O(N) where N is the number of chars inside the input String passed as
   * parameter.
   */
  public boolean evaluate(String input) {
    validateInput(input);
    Map<String, Integer> chars = countCharAppearances(input);
    return !containsDuplicatedChars(chars);
  }
  
  /**
   * Faster solution to this problem. This solution is based on one important detail, we are
   * assuming ASCII as charset. This solution is also iterative and changes the HashMap
   * implementation with an array with a size equivalent to the maximum different chars you can
   * find in ASCII. The complexity order of this algorithm in time terms is O(N), like the previous
   * algorithm. In space terms, the complexity order of this algorithm is O(1).
   */
  public boolean evaluate2(String input) {
    validateInput(input);

    int[] chars = new int[256];
    for (char c : input.toCharArray()) {
      if (chars[c] >= 1) {
        return false;
      } else {
        chars[c]++;
      }
    }
    return true;
  }

  private Map<String, Integer> countCharAppearances(String input) {
    Map<String, Integer> chars = new HashMap<String, Integer>();
    for (char c : input.toCharArray()) {
      countChar(chars, String.valueOf(c));
    }
    return chars;
  }

  private void countChar(Map<String, Integer> chars, String c) {
    Integer newCount = chars.containsKey(c) ? chars.get(c) + 1 : 1;
    chars.put(c, newCount);
  }

  private boolean containsDuplicatedChars(Map<String, Integer> chars) {
    boolean duplicatedChar = false;
    for (Integer counter : chars.values()) {
      if (counter > 1) {
        duplicatedChar = true;
        break;
      }
    }
    return duplicatedChar;
  }

  private void validateInput(String input) {
    if (input == null) {
      throw new IllegalArgumentException("You can't pass a null instance as parameter.");
    }
  }
}
