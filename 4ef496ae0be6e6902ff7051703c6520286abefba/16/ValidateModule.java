/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.model.modules.validate;

import com.databasepreservation.common.observer.ValidationObserver;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.modules.ExceptionNormalizer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface ValidateModule extends ExceptionNormalizer {

  /**
   * The reporter is set specifically for each module
   *
   * @param reporter
   *          The reporter that should be used by this ValidateModule
   */
  void setOnceReporter(Reporter reporter);

  void setObserver(ValidationObserver observer);

  /**
   *
   *
   * @throws ModuleException
   *          Generic module exception
   */
  boolean validate() throws ModuleException;
}
