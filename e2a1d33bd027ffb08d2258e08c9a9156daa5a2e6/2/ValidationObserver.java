/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.common;

import com.databasepreservation.model.reporters.ValidationReporterStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface ValidationObserver {
  void notifyStartValidationModule(String moduleName, String ID);

  void notifyValidationStep(String moduleName, String step, ValidationReporterStatus status);

  void notifyMessage(String moduleName, String ID, String message, ValidationReporterStatus status);

  void notifyFinishValidationModule(String moduleName, ValidationReporterStatus status);

  void notifyComponent(String ID, ValidationReporterStatus status);

  void notifyElementValidating(String ID, String path);

  void notifyIndicators(int passed, int ok, int failed, int errors, int warnings, int skipped);

  void notifyValidationProcessFinish(boolean value);

  void notifyValidationProgressSparse(int numberOfRows);

  void notifyElementValidationFinish(String ID, String path, ValidationReporterStatus status);
}
