package com.databasepreservation.modules.siard.validate.component.factories;

import com.databasepreservation.Constants;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.components.ValidatorComponent;
import com.databasepreservation.model.components.ValidatorComponentFactory;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.modules.siard.validate.component.metadata.MetadataForeignKeyValidator;
import com.databasepreservation.modules.siard.validate.component.metadata.MetadataReferenceValidator;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataReferenceComponentFactory implements ValidatorComponentFactory {
  private final String MODULE_NAME = Constants.COMPONENT_METADATA_REFERENCE;

  @Override
  public String getComponentName() {
    return MODULE_NAME;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isFirst() {
    return false;
  }

  @Override
  public String next() {
    return Constants.COMPONENT_METADATA_CANDIDATE_KEY;
  }

  @Override
  public ValidatorComponent buildComponent(Reporter reporter) throws ModuleException {
    return new MetadataReferenceValidator(MODULE_NAME);
  }
}
