package com.databasepreservation.model.modules;

import java.util.Map;

import javax.naming.OperationNotSupportedException;

import com.databasepreservation.model.exception.LicenseNotAcceptedException;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.Parameters;

import net.xeoh.plugins.base.Plugin;

/**
 * Defines a factory used to create Import and Export Modules. This factory
 * should also be able to inform the parameters needed to create a new import or
 * export module.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface DatabaseModuleFactory extends Plugin {
  boolean producesImportModules();

  boolean producesExportModules();

  String getModuleName();

  Map<String, Parameter> getAllParameters();

  Parameters getImportModuleParameters() throws OperationNotSupportedException;

  Parameters getExportModuleParameters() throws OperationNotSupportedException;

  DatabaseImportModule buildImportModule(Map<Parameter, String> parameters) throws OperationNotSupportedException,
    LicenseNotAcceptedException;

  DatabaseExportModule buildExportModule(Map<Parameter, String> parameters) throws OperationNotSupportedException,
    LicenseNotAcceptedException;

  class ExceptionBuilder {
    public static OperationNotSupportedException OperationNotSupportedExceptionForImportModule() {
      return new OperationNotSupportedException("Import module not available");
    }

    public static OperationNotSupportedException OperationNotSupportedExceptionForExportModule() {
      return new OperationNotSupportedException("Export module not available");
    }
  }
}
