/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.modules.siard.in.input;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.managers.ModuleConfigurationManager;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.configuration.ModuleConfiguration;
import com.databasepreservation.model.modules.filters.DatabaseFilterModule;
import com.databasepreservation.model.reporters.Reporter;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.modules.DefaultExceptionNormalizer;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;
import com.databasepreservation.modules.siard.in.content.ContentImportStrategy;
import com.databasepreservation.modules.siard.in.metadata.MetadataImportStrategy;
import com.databasepreservation.modules.siard.in.read.ReadStrategy;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARDImportDefault implements DatabaseImportModule {
  private final String moduleName;
  private final ReadStrategy readStrategy;
  private final SIARDArchiveContainer mainContainer;
  private final ContentImportStrategy contentStrategy;
  private final MetadataImportStrategy metadataStrategy;
  private final Map<String, String> importProperties;
  private static final Logger LOGGER = LoggerFactory.getLogger(SIARDImportDefault.class);

  public SIARDImportDefault(String moduleName, ContentImportStrategy contentStrategy,
    SIARDArchiveContainer mainContainer, ReadStrategy readStrategy, MetadataImportStrategy metadataStrategy,
    Map<String, String> importProperties) {
    this.moduleName = moduleName;
    this.readStrategy = readStrategy;
    this.mainContainer = mainContainer;
    this.contentStrategy = contentStrategy;
    this.metadataStrategy = metadataStrategy;
    this.importProperties = importProperties;
  }

  @Override
  public DatabaseFilterModule migrateDatabaseTo(DatabaseFilterModule handler) throws ModuleException {
    final ModuleConfiguration moduleConfiguration = ModuleConfigurationManager.getInstance().getModuleConfiguration();
    readStrategy.setup(mainContainer);
    LOGGER.info("Importing SIARD version {}", mainContainer.getVersion().getDisplayName());
    handler.initDatabase();
    try {
      metadataStrategy.loadMetadata(readStrategy, mainContainer, moduleConfiguration);

      DatabaseStructure dbStructure = metadataStrategy.getDatabaseStructure();

      handler.handleStructure(dbStructure);

      contentStrategy.importContent(handler, mainContainer, dbStructure, moduleConfiguration);

      handler.updateModuleConfiguration(moduleName, importProperties, null);

      handler.finishDatabase();
    } finally {
      readStrategy.finish(mainContainer);
    }
    return null;
  }

  /**
   * Provide a reporter through which potential conversion problems should be
   * reported. This reporter should be provided only once for the export module
   * instance.
   *
   * @param reporter
   *          The initialized reporter instance.
   */
  @Override
  public void setOnceReporter(Reporter reporter) {
    metadataStrategy.setOnceReporter(reporter);
  }

  @Override
  public ModuleException normalizeException(Exception exception, String contextMessage) {
    return DefaultExceptionNormalizer.getInstance().normalizeException(exception, contextMessage);
  }
}
