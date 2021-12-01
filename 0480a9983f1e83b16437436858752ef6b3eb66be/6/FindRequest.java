/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.index;

import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.sort.Sorter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sublist.Sublist;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to a find operation.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FindRequest extends CountRequest {

  private static final long serialVersionUID = 5997470558754294987L;

  /** Sorter. */
  public Sorter sorter;
  /** Sublist (paging). */
  public Sublist sublist;
  /** Facets to return. */
  public Facets facets;
  /** For CSV results, export only facets? */
  public boolean exportFacets;
  /** The filename for exported CSV. */
  public String filename;
  /** The index fields to return and use to construct the indexed object. */
  public List<String> fieldsToReturn;

  /**
   * Constructor.
   */
  public FindRequest() {
    this(null, new Filter(), new Sorter(), new Sublist(), new Facets());
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
                     final Facets facets) {
    this(classToReturn, filter, sorter, sublist, facets, false, "export.csv", new ArrayList<>());
  }

  /**
   * Constructor.
   * 
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   * @param exportFacets
   *          for CSV results, export only facets?
   * @param filename
   *          the filename for exported CSV.
   * @param fieldsToReturn
   *          the index fields to return.
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
                     final Facets facets, final boolean exportFacets, final String filename,
                     final List<String> fieldsToReturn) {
    super(classToReturn, filter);
    this.sorter = sorter;
    this.sublist = sublist;
    this.facets = facets;
    this.exportFacets = exportFacets;
    this.filename = filename;
    this.fieldsToReturn = fieldsToReturn;
  }

}
