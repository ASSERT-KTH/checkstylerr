/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class LongRangeFilterParameter extends RangeFilterParameter<Long> {
  private static final long serialVersionUID = -5658723022959992610L;

  public LongRangeFilterParameter() {
    super();
  }

  public LongRangeFilterParameter(RangeFilterParameter<Long> rangeFilterParameter) {
    super(rangeFilterParameter);
  }

  public LongRangeFilterParameter(String name, Long fromValue, Long toValue) {
    super(name, fromValue, toValue);
  }

  public LongRangeFilterParameter(String name, String fromValue, String toValue) {
    super(name, parseInput(fromValue), parseInput(toValue));
  }

  private static Long parseInput(String value) {
    if (value != null && value.length() > 0) {
      return Long.parseLong(value);
    }
    return null;
  }
}
