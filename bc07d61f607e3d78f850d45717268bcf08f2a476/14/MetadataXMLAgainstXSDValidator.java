package com.databasepreservation.modules.siard.validate.component.metadata;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.reporters.ValidationReporter;
import com.databasepreservation.modules.siard.validate.component.ValidatorComponentImpl;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataXMLAgainstXSDValidator extends ValidatorComponentImpl {
  private static final String MODULE_NAME = "Requirements for metadata";
  private static final String M_50 = "5.0";
  private static final String M_501 = "M_5.0-1";

  public static MetadataXMLAgainstXSDValidator newInstance() {
    return new MetadataXMLAgainstXSDValidator();
  }

  private MetadataXMLAgainstXSDValidator() {
  }

  @Override
  public boolean validate() throws ModuleException {
    if (preValidationRequirements())
      return false;

    getValidationReporter().moduleValidatorHeader(M_50, MODULE_NAME);
    if (validateXMLAgainstXSD()) {
      getValidationReporter().validationStatus(M_501, ValidationReporter.Status.OK);
    } else {
      closeZipFile();
      reportValidations(M_501, MODULE_NAME);
      return false;
    }

    getValidationReporter().moduleValidatorFinished(MODULE_NAME, ValidationReporter.Status.PASSED);
    closeZipFile();
    return true;
  }

  /**
   * M_5.0-1 The schema definition metadata.xsd must be complied with for the
   * metadata.xml file. This means that metadata.xml must be capable of being
   * positively validated against metadata.xsd.
   *
   * @return true if valid otherwise false
   */
  private boolean validateXMLAgainstXSD() {
    InputStream XSDInputStream = getZipInputStream(validatorPathStrategy.getMetadataXSDPath());
    InputStream XMLInputStream = getZipInputStream(validatorPathStrategy.getMetadataXMLPath());

    Source schemaFile = new StreamSource(XSDInputStream);
    Source xmlFile = new StreamSource(XMLInputStream);

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema;
    try {
      schema = schemaFactory.newSchema(schemaFile);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);
    } catch (SAXException | IOException e) {
      setError(M_501, e.getMessage());
      return false;
    }

    return true;
  }

}