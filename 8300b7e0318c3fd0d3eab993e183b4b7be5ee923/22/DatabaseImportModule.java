/**
 *
 */
package com.databasepreservation.model.modules;

import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.InvalidDataException;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface DatabaseImportModule {
  /**
   * Import the database model.
   *
   * @param databaseExportModule
   *          The database model handler to be called when importing the database.
   * @return Return null unless this DatabaseImportModule also implements
   *         DatabaseExportModule
   * @throws UnknownTypeException
   *           a type used in the original database structure is unknown and
   *           cannot be mapped
   * @throws InvalidDataException
   *           the database data is not valid
   * @throws ModuleException
   *           generic module exception
   */
  DatabaseExportModule migrateDatabaseTo(DatabaseExportModule databaseExportModule) throws ModuleException;

  /**
   * Provide a reporter through which potential conversion problems should be
   * reported. This reporter should be provided only once for the export module
   * instance.
   *
   * @param reporter
   *          The initialized reporter instance.
   */
  void setOnceReporter(Reporter reporter);
}
