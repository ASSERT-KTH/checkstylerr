/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.modules.siard.in.input;

import java.nio.file.Path;

import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.configuration.ModuleConfiguration;
import com.databasepreservation.modules.siard.common.SIARDArchiveContainer;
import com.databasepreservation.modules.siard.common.path.MetadataPathStrategy;
import com.databasepreservation.modules.siard.common.path.SIARD1MetadataPathStrategy;
import com.databasepreservation.modules.siard.constants.SIARDConstants;
import com.databasepreservation.modules.siard.in.content.ContentImportStrategy;
import com.databasepreservation.modules.siard.in.content.SIARD1ContentImportStrategy;
import com.databasepreservation.modules.siard.in.metadata.MetadataImportStrategy;
import com.databasepreservation.modules.siard.in.metadata.SIARD1MetadataImportStrategy;
import com.databasepreservation.modules.siard.in.path.ContentPathImportStrategy;
import com.databasepreservation.modules.siard.in.path.SIARD1ContentPathImportStrategy;
import com.databasepreservation.modules.siard.in.read.ReadStrategy;
import com.databasepreservation.modules.siard.in.read.ZipReadStrategy;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARD1ImportModule {
  private static final String moduleName = "siard-1";
  private final ModuleConfiguration moduleConfiguration;
  private final ReadStrategy readStrategy;
  private final SIARDArchiveContainer mainContainer;
  private final MetadataImportStrategy metadataStrategy;
  private final ContentImportStrategy contentStrategy;

  public SIARD1ImportModule(ModuleConfiguration moduleConfiguration, Path siardPackagePath) {
    Path siardPackageNormalizedPath = siardPackagePath.toAbsolutePath().normalize();
    this.moduleConfiguration = moduleConfiguration;
    readStrategy = new ZipReadStrategy();
    mainContainer = new SIARDArchiveContainer(SIARDConstants.SiardVersion.V1_0, siardPackageNormalizedPath,
      SIARDArchiveContainer.OutputContainerType.MAIN);

    ContentPathImportStrategy contentPathStrategy = new SIARD1ContentPathImportStrategy();
    contentStrategy = new SIARD1ContentImportStrategy(readStrategy, contentPathStrategy);

    MetadataPathStrategy metadataPathStrategy = new SIARD1MetadataPathStrategy();
    metadataStrategy = new SIARD1MetadataImportStrategy(metadataPathStrategy, contentPathStrategy);
  }

  public DatabaseImportModule getDatabaseImportModule() {
    return new SIARDImportDefault(moduleName, moduleConfiguration, contentStrategy, mainContainer, readStrategy, metadataStrategy);
  }
}
