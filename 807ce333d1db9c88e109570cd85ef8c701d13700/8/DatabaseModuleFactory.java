package com.databasepreservation.model.modules;

import java.util.Map;

import com.databasepreservation.model.exception.LicenseNotAcceptedException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
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

  Parameters getImportModuleParameters() throws UnsupportedModuleException;

  Parameters getExportModuleParameters() throws UnsupportedModuleException;

  DatabaseImportModule buildImportModule(Map<Parameter, String> parameters) throws UnsupportedModuleException,
    LicenseNotAcceptedException;

  DatabaseExportModule buildExportModule(Map<Parameter, String> parameters) throws UnsupportedModuleException,
    LicenseNotAcceptedException;

  class ExceptionBuilder {
    public static UnsupportedModuleException UnsupportedModuleExceptionForImportModule() {
      return new UnsupportedModuleException("Import module not available");
    }

    public static UnsupportedModuleException UnsupportedModuleExceptionForExportModule() {
      return new UnsupportedModuleException("Export module not available");
    }
  }
}
