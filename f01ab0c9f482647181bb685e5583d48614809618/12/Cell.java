/**
 *
 */
package com.databasepreservation.model.data;

/**
 * @author Luis Faria
 *         <p>
 *         Abstract container of data
 */
public abstract class Cell {
  private String id;

  /**
   * Abstract Cell constructor
   *
   * @param id
   *          the cell id, equal to 'tableId.columnId.rowIndex'
   */
  public Cell(String id) {
    this.id = id;
  }

  /**
   * @return the cell id, equal to 'tableId.columnId.rowIndex'
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the cell id, equal to 'tableId.columnId.rowIndex'
   */
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "Cell{" + "id='" + id + '\'' + '}';
  }
}
