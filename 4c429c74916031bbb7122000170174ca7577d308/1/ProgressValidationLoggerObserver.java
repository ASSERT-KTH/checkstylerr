/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.model.modules.validate;

import com.databasepreservation.model.reporters.ValidationReporterStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.ValidationObserver;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressValidationLoggerObserver implements ValidationObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProgressValidationLoggerObserver.class);

  @Override
  public void notifyStartValidationModule(String moduleName, String ID) {
    LOGGER.info("Start validation of: {} - {}", ID, moduleName);
  }

  @Override
  public void notifyValidationStep(String moduleName, String step, ValidationReporterStatus status) {
    LOGGER.info("{}: [{}]", step, status);
  }

  @Override
  public void notifyMessage(String moduleName, String message, ValidationReporterStatus status) {
    LOGGER.info("{}: [{}] - {}", moduleName, status, message);
  }

  @Override
  public void notifyFinishValidationModule(String moduleName, ValidationReporterStatus status) {
    LOGGER.info("{} [{}]", moduleName, status);
  }

  @Override
  public void notifyComponent(String ID, ValidationReporterStatus status) {
    LOGGER.info("{}: [{}] ", ID, status);
  }

  @Override
  public void notifyElementValidating(String path) {
    LOGGER.info("{}", path);
  }

  @Override
  public void notifyNumberOfWarnings(int warnings) {
    LOGGER.info("Number of warnings [{}]", warnings);
  }

  @Override
  public void notifyValidationProcessFinish(boolean value) {
    LOGGER.info("Validation process finished the SIARD is {}.", transformValidationStatus(value));
  }

  private String transformValidationStatus(boolean value) {
    if (value) return "valid";
    else return "not valid";
  }
}
