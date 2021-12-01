/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.index;

import com.databasepreservation.common.client.index.filter.Filter;

import java.io.Serializable;

/**
 * A request to a count operation.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CountRequest implements Serializable {

  private static final long serialVersionUID = -6793510712321710035L;

  /** Class name of resources to return. */
  public String classToReturn;
  /** Filter. */
  public Filter filter;

  /**
   * Constructor.
   */
  public CountRequest() {
    this(null, new Filter());
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   */
  public CountRequest(final String classToReturn, final Filter filter) {
    this.classToReturn = classToReturn;
    this.filter = filter;
  }

}
