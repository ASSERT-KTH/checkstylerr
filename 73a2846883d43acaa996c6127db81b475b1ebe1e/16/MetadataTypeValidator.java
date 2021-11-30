package com.databasepreservation.modules.siard.validate.component.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import com.databasepreservation.model.reporters.ValidationReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.databasepreservation.Constants;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.utils.XMLUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataTypeValidator extends MetadataValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataTypeValidator.class);
  private final String MODULE_NAME;
  private static final String M_53 = "5.3";
  private static final String M_531 = "M_5.3-1";
  private static final String M_531_1 = "M_5.3-1-1";
  private static final String M_531_2 = "M_5.3-1-2";
  private static final String M_531_5 = "M_5.3-1-5";
  private static final String M_531_6 = "M_5.3-1-6";
  private static final String M_531_10 = "M_5.3-1-10";

  private List<Element> typesList = new ArrayList<>();

  public MetadataTypeValidator(String moduleName) {
    this.MODULE_NAME = moduleName;
    setCodeListToValidate(M_531, M_531_1, M_531_2, M_531_5, M_531_6, M_531_10);
  }

  @Override
  public boolean validate() throws ModuleException {
    observer.notifyStartValidationModule(MODULE_NAME, M_53);
    if (preValidationRequirements()) {
      LOGGER.debug("Failed to validate the pre-requirements for {}", MODULE_NAME);
      return false;
    }

    getValidationReporter().moduleValidatorHeader(M_53, MODULE_NAME);

    if (!validateMandatoryXSDFields(M_531, TYPE_TYPE, "/ns:siardArchive/ns:schemas/ns:schema/ns:types/ns:type")) {
      reportValidations(M_531, MODULE_NAME);
      closeZipFile();
      return false;
    }

    if (!readXMLMetadataTypeLevel()) {
      reportValidations(M_531, MODULE_NAME);
      closeZipFile();
      return false;
    }
    closeZipFile();

    // there is no need to continue the validation if no have types in any schema
    if (typesList.isEmpty()) {
      getValidationReporter().skipValidation(M_531, "Database has no types");
      observer.notifyValidationStep(MODULE_NAME, M_531, ValidationReporter.Status.SKIPPED);
      metadataValidationPassed(MODULE_NAME);
      return true;
    }

    if (reportValidations(MODULE_NAME)) {
      metadataValidationPassed(MODULE_NAME);
      return true;
    }
    return false;
  }

  private boolean readXMLMetadataTypeLevel() {
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema/ns:types/ns:type", XPathConstants.NODESET,
        Constants.NAMESPACE_FOR_METADATA);

      for (int i = 0; i < nodes.getLength(); i++) {
        Element type = (Element) nodes.item(i);
        typesList.add(type);

        String schema = XMLUtils.getChildTextContext((Element) type.getParentNode().getParentNode(), "name");

        String name = XMLUtils.getChildTextContext(type, Constants.NAME);
        String category = XMLUtils.getChildTextContext(type, Constants.CATEGORY);
        String instantiable = XMLUtils.getChildTextContext(type, Constants.TYPE_INSTANTIABLE);
        String finalField = XMLUtils.getChildTextContext(type, Constants.TYPE_FINAL);
        String description = XMLUtils.getChildTextContext(type, Constants.DESCRIPTION);

        String path = buildPath(Constants.SCHEMA, schema, Constants.TYPE, Integer.toString(i));
        if (!validateTypeName(name, path))
          continue; // next type

        path = buildPath(Constants.SCHEMA, schema, Constants.TYPE, name);
        if (!validateTypeCategory(category, path))
          continue; // next type

        if (!validateTypeInstantiable(instantiable, path))
          continue; // next type

        if (!validateTypefinal(finalField, path))
          continue; // next type

        validateTypeDescription(description, path);
      }

    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      String errorMessage = "Unable to read types from SIARD file";
      setError(M_531, errorMessage);
      LOGGER.debug(errorMessage, e);
      return false;
    }

    return true;
  }

  /**
   * M_5.3-1-1 The type name in the schema must not be empty. ERROR when it is
   * empty
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypeName(String typeName, String path) {
    return validateXMLField(M_531_1, typeName, Constants.NAME, true, false, path);
  }

  /**
   * M_5.3-1-2 The type category in the schema must not be empty. ERROR when it is
   * empty
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypeCategory(String category, String path) {
    return validateXMLField(M_531_2, category, Constants.CATEGORY, true, false, path);
  }

  /**
   * M_5.3-1-5 The type instantiable field in the schema must not be empty. ERROR
   * when it is empty
   *
   * @return true if valid otherwise false
   */

  private boolean validateTypeInstantiable(String instantiable, String path) {
    return validateXMLField(M_531_5, instantiable, Constants.TYPE_INSTANTIABLE, true, false, path);
  }

  /**
   * M_5.3-1-6 The type final field in the schema must not be empty. ERROR when it
   * is empty
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypefinal(String typeFinal, String path) {
    return validateXMLField(M_531_6, typeFinal, Constants.TYPE_FINAL, true, false, path);
  }

  /**
   * M_5.3-1-10 The type description field in the schema must not be must not be
   * less than 3 characters. WARNING if it is less than 3 characters
   *
   */
  private void validateTypeDescription(String description, String path) {
    validateXMLField(M_531_10, description, Constants.DESCRIPTION, false, true, path);
  }

}
