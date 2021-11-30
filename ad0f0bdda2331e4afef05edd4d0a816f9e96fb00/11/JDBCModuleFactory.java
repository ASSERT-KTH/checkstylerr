package com.databasepreservation.modules.jdbc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.LicenseNotAcceptedException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
import com.databasepreservation.model.modules.DatabaseExportModule;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.Parameters;
import com.databasepreservation.modules.jdbc.in.JDBCImportModule;
import com.databasepreservation.modules.jdbc.out.JDBCExportModule;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class JDBCModuleFactory implements DatabaseModuleFactory {
  private static final Parameter driver = new Parameter()
    .shortName("d")
    .longName("driver")
    .description(
      "the name of the the JDBC driver class. For more info about this refer to the website or the README file")
    .hasArgument(true).setOptionalArgument(false).required(true);

  private static final Parameter connection = new Parameter().shortName("c").longName("connection")
    .description("the connection url to use in the connection").hasArgument(true).setOptionalArgument(false)
    .required(true);

  // additional parameters. unused
  private static final Parameter params = new Parameter().shortName("p").longName("parameter").required(false)
    .valueIfNotSet(null).hasArgument(true).setOptionalArgument(false);

  private Reporter reporter;

  private JDBCModuleFactory() {
  }

  public JDBCModuleFactory(Reporter reporter) {
    this.reporter = reporter;
  }

  @Override
  public boolean producesImportModules() {
    return true;
  }

  @Override
  public boolean producesExportModules() {
    return true;
  }

  @Override
  public String getModuleName() {
    return "jdbc";
  }

  @Override
  public Map<String, Parameter> getAllParameters() {
    HashMap<String, Parameter> parameterHashMap = new HashMap<String, Parameter>();
    parameterHashMap.put(driver.longName(), driver);
    parameterHashMap.put(connection.longName(), connection);
    // parameterHashMap.put(params.longName(), params);
    return parameterHashMap;
  }

  @Override
  public Parameters getImportModuleParameters() throws UnsupportedModuleException {
    return new Parameters(Arrays.asList(driver, connection), null);
  }

  @Override
  public Parameters getExportModuleParameters() throws UnsupportedModuleException {
    return new Parameters(Arrays.asList(driver, connection), null);
  }

  @Override
  public DatabaseImportModule buildImportModule(Map<Parameter, String> parameters) throws UnsupportedModuleException,
    LicenseNotAcceptedException {
    String pDriver = parameters.get(driver);
    String pConnection = parameters.get(connection);
    // String pParams = parameters.get(params);

    reporter.importModuleParameters(this.getModuleName(), "driver", pDriver, "connection", pConnection);
    return new JDBCImportModule(pDriver, pConnection);
  }

  @Override
  public DatabaseExportModule buildExportModule(Map<Parameter, String> parameters) throws UnsupportedModuleException,
    LicenseNotAcceptedException {
    String pDriver = parameters.get(driver);
    String pConnection = parameters.get(connection);
    // String pParams = parameters.get(params);

    reporter.exportModuleParameters(this.getModuleName(), "driver", pDriver, "connection", pConnection);
    return new JDBCExportModule(pDriver, pConnection);
  }
}
