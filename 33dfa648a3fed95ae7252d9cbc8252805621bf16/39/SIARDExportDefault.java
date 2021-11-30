/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.modules.siard.out.output;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.Constants;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.modules.DatabaseExportModule;
import com.databasepreservation.model.modules.configuration.ModuleConfiguration;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.modules.DefaultExceptionNormalizer;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;
import com.databasepreservation.modules.siard.common.SIARDValidator;
import com.databasepreservation.modules.siard.constants.SIARDConstants;
import com.databasepreservation.modules.siard.out.content.ContentExportStrategy;
import com.databasepreservation.modules.siard.out.metadata.MetadataExportStrategy;
import com.databasepreservation.modules.siard.out.write.WriteStrategy;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARDExportDefault implements DatabaseExportModule {
  private final SIARDArchiveContainer mainContainer;
  private final WriteStrategy writeStrategy;
  private final MetadataExportStrategy metadataStrategy;
  private final ContentExportStrategy contentStrategy;

  private DatabaseStructure dbStructure;
  private SchemaStructure currentSchema;
  private TableStructure currentTable;
  private Map<String, String> descriptiveMetadata;
  private Reporter reporter;

  private ModuleConfiguration moduleConfiguration = null;
  private boolean validate = false;

  private static final Logger LOGGER = LoggerFactory.getLogger(SIARDExportDefault.class);

  public SIARDExportDefault(ContentExportStrategy contentStrategy, SIARDArchiveContainer mainContainer,
    WriteStrategy writeStrategy, MetadataExportStrategy metadataStrategy, Map<String, String> descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
    this.contentStrategy = contentStrategy;
    this.mainContainer = mainContainer;
    this.writeStrategy = writeStrategy;
    this.metadataStrategy = metadataStrategy;
  }

  public SIARDExportDefault(ContentExportStrategy contentStrategy, SIARDArchiveContainer mainContainer,
    WriteStrategy writeStrategy, MetadataExportStrategy metadataStrategy, Map<String, String> descriptiveMetadata,
    boolean validate) {
    this.descriptiveMetadata = descriptiveMetadata;
    this.contentStrategy = contentStrategy;
    this.mainContainer = mainContainer;
    this.writeStrategy = writeStrategy;
    this.metadataStrategy = metadataStrategy;
    this.validate = validate;
  }

  @Override
  public void initDatabase() throws ModuleException {
    writeStrategy.setup(mainContainer);
    LOGGER.info("Exporting SIARD version {}", mainContainer.getVersion().getDisplayName());
  }

  @Override
  public void setIgnoredSchemas(Set<String> ignoredSchemas) {
    // nothing to do
  }

  @Override
  public void handleStructure(DatabaseStructure structure) throws ModuleException {
    if (structure == null) {
      throw new ModuleException().withMessage("Database structure must not be null");
    }

    dbStructure = structure;

    // update database structure with descriptive metadata from parameters
    if (descriptiveMetadata != null) {
      String descriptionFromMetadata = descriptiveMetadata.get(SIARDConstants.DESCRIPTIVE_METADATA_DESCRIPTION);
      if (StringUtils.isBlank(dbStructure.getDescription())
        || !descriptionFromMetadata.equals(Constants.UNSPECIFIED_METADATA_VALUE)) {
        dbStructure.setDescription(descriptionFromMetadata);
      }

      dbStructure.setArchiver(descriptiveMetadata.get(SIARDConstants.DESCRIPTIVE_METADATA_ARCHIVER));
      dbStructure.setArchiverContact(descriptiveMetadata.get(SIARDConstants.DESCRIPTIVE_METADATA_ARCHIVER_CONTACT));
      dbStructure.setDataOwner(descriptiveMetadata.get(SIARDConstants.DESCRIPTIVE_METADATA_DATA_OWNER));
      dbStructure
        .setDataOriginTimespan(descriptiveMetadata.get(SIARDConstants.DESCRIPTIVE_METADATA_DATA_ORIGIN_TIMESPAN));
      dbStructure.setClientMachine(descriptiveMetadata.get(SIARDConstants.DESCRIPTIVE_METADATA_CLIENT_MACHINE));
    }
  }

  @Override
  public void handleDataOpenSchema(String schemaName) throws ModuleException {
    currentSchema = dbStructure.getSchemaByName(schemaName);

    if (currentSchema == null) {
      throw new ModuleException().withMessage("Couldn't find schema with name: " + schemaName);
    }

    contentStrategy.openSchema(currentSchema);
  }

  @Override
  public void handleDataOpenTable(String tableId) throws ModuleException {
    currentTable = dbStructure.getTableById(tableId);

    if (currentTable == null) {
      throw new ModuleException().withMessage("Couldn't find table with id: " + tableId);
    }

    contentStrategy.openTable(currentTable);
  }

  @Override
  public void handleDataCloseTable(String tableId) throws ModuleException {
    currentTable = dbStructure.getTableById(tableId);

    if (currentTable == null) {
      throw new ModuleException().withMessage("Couldn't find table with id: " + tableId);
    }

    contentStrategy.closeTable(currentTable);
  }

  @Override
  public void handleDataCloseSchema(String schemaName) throws ModuleException {
    currentSchema = dbStructure.getSchemaByName(schemaName);

    if (currentSchema == null) {
      throw new ModuleException().withMessage("Couldn't find schema with name: " + schemaName);
    }

    contentStrategy.closeSchema(currentSchema);
  }

  @Override
  public void handleDataRow(Row row) throws ModuleException {
    contentStrategy.tableRow(row);
  }

  @Override
  public void finishDatabase() throws ModuleException {
    metadataStrategy.writeMetadataXML(dbStructure, mainContainer, writeStrategy);
    metadataStrategy.writeMetadataXSD(dbStructure, mainContainer, writeStrategy);
    writeStrategy.finish(mainContainer);

    if (validate) {
      SIARDValidator validator = new SIARDValidator(mainContainer, writeStrategy);
      validator.setReporter(reporter);
      validator.validateSIARD();
    }
  }

  @Override
  public void updateModuleConfiguration(String moduleName, Map<String, String> properties,
    Map<String, String> remoteProperties) {
    // do nothing
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
    this.reporter = reporter;
    contentStrategy.setOnceReporter(reporter);
    metadataStrategy.setOnceReporter(reporter);
  }

  @Override
  public ModuleException normalizeException(Exception exception, String contextMessage) {
    return DefaultExceptionNormalizer.getInstance().normalizeException(exception, contextMessage);
  }
}
