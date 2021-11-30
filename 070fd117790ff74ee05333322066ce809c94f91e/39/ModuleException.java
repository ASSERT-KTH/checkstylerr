/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
/**
 *
 */
package com.databasepreservation.model.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Luis Faria
 */
public class ModuleException extends Exception {

  /**
         *
         */
  private static final long serialVersionUID = -200829472177640163L;

  private HashMap<String, Throwable> errors = null;

  /**
   * Create an empty generic module exception
   */
  public ModuleException() {
  }

  /**
   * Create a generic module exception
   *
   * @param mesg
   *          the error message
   */
  public ModuleException(String mesg) {
    super(mesg);
  }

  /**
   * Create a generic module exception specifying the cause
   *
   * @param cause
   *          the underlying error
   */
  public ModuleException(Throwable cause) {
    super(cause);
  }

  /**
   * Create a generic module exception specifying a message and the cause
   *
   * @param message
   *          the error message
   * @param cause
   *          the underlying error
   */
  public ModuleException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a generic module exception with a map of error messages and causes
   *
   * @param errors
   *          the errors messages and causes
   */
  public ModuleException(Map<String, Throwable> errors) {
    this.errors = new HashMap<>(errors);
  }

  /**
   * Get the error messages as defined by the specialized constructor
   *
   * @return The errors or null if none
   */
  public Map<String, Throwable> getModuleErrors() {
    return errors;
  }

}
