/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.managers;

import com.databasepreservation.model.modules.configuration.ModuleConfiguration;
import com.databasepreservation.utils.ModuleConfigurationUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ModuleConfigurationManager {

  private ModuleConfiguration moduleConfiguration = null;
  private boolean initialized = false;
  private static ModuleConfigurationManager instance = null;

  public static ModuleConfigurationManager getInstance() {
    if (instance == null) {
      instance = new ModuleConfigurationManager();
    }

    return instance;
  }

  public void setup(ModuleConfiguration configuration) {
    moduleConfiguration = configuration;
    initialized = true;
  }

  public ModuleConfiguration getModuleConfiguration() {
    if (isInitialized()) {
      return moduleConfiguration;
    } else {
      return ModuleConfigurationUtils.getDefaultModuleConfiguration();
    }
  }

  public boolean isInitialized() {
    return initialized;
  }

  public static void destroy() {
    instance = null;
  }
}
