package com.databasepreservation.modules.siard.validate.component.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.databasepreservation.Constants;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.reporters.ValidationReporter;
import com.databasepreservation.utils.XMLUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataCheckConstraintValidator extends MetadataValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataCheckConstraintValidator.class);
  private final String MODULE_NAME;
  private static final String M_512 = "5.12";
  private static final String M_512_1 = "M_5.12-1";
  private static final String M_512_1_1 = "M_5.12-1-1";
  private static final String M_512_1_3 = "M_5.12-1-3";

  private List<Element> checkConstraintList = new ArrayList<>();
  private Set<String> checkDuplicates = new HashSet<>();

  public MetadataCheckConstraintValidator(String moduleName) {
    this.MODULE_NAME = moduleName;
    setCodeListToValidate(M_512_1, M_512_1_1, M_512_1_3);
  }

  @Override
  public boolean validate() throws ModuleException {
    observer.notifyStartValidationModule(MODULE_NAME, M_512);
    if (preValidationRequirements()) {
      LOGGER.debug("Failed to validate the pre-requirements for {}", MODULE_NAME);
      return false;
    }

    getValidationReporter().moduleValidatorHeader(M_512, MODULE_NAME);

    if (!validateMandatoryXSDFields(M_512_1, CHECK_CONSTRAINT_TYPE,
            "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table/ns:checkConstraints/ns:checkConstraint")) {
      reportValidations(M_512_1, MODULE_NAME);
      closeZipFile();
      return false;
    }

    if (!readXMLMetadataCheckConstraint()) {
      reportValidations(M_512_1, MODULE_NAME);
      closeZipFile();
      return false;
    }
    closeZipFile();

    if (checkConstraintList.isEmpty()) {
      getValidationReporter().skipValidation(M_512_1, "Database has no check constraint");
      metadataValidationPassed(MODULE_NAME);
      return true;
    }

    if (reportValidations(MODULE_NAME)) {
      metadataValidationPassed(MODULE_NAME);
      return true;
    }
    return false;
  }

  private boolean readXMLMetadataCheckConstraint() {
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table/ns:checkConstraints/ns:checkConstraint",
        XPathConstants.NODESET, Constants.NAMESPACE_FOR_METADATA);

      for (int i = 0; i < nodes.getLength(); i++) {
        Element checkConstraint = (Element) nodes.item(i);
        checkConstraintList.add(checkConstraint);
        Element tableElement = (Element) checkConstraint.getParentNode().getParentNode();
        Element schemaElement = (Element) tableElement.getParentNode().getParentNode();

        String schema = XMLUtils.getChildTextContext(schemaElement, Constants.NAME);
        String table = XMLUtils.getChildTextContext(tableElement, Constants.NAME);

        String name = XMLUtils.getChildTextContext(checkConstraint, Constants.NAME);
        String path = buildPath(Constants.SCHEMA, schema, Constants.TABLE, table, Constants.NAME, name);
        if (!validateCheckConstraintName(name, path))
          break;

        String condition = XMLUtils.getChildTextContext(checkConstraint, Constants.CONDITION);
        // M_512_1
        if (!validateXMLField(M_512_1, condition, Constants.CONDITION, true, false, path)) {
          return false;
        }

        String description = XMLUtils.getChildTextContext(checkConstraint, Constants.DESCRIPTION);
        if (!validateCheckConstraintDescription(description, path))
          break;
      }

    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      String errorMessage = "Unable to read check constraint from SIARD file";
      setError(M_512_1, errorMessage);
      LOGGER.debug(errorMessage, e);
      return false;
    }
    return true;
  }

  /**
   * M_5.12-1-1 The Check Constraint name in SIARD file must be unique and exist.
   * ERROR if not unique or is null.
   *
   * @return true if valid otherwise false
   */

  private boolean validateCheckConstraintName(String name, String path) {
    // M_512_1
    if (!validateXMLField(M_512_1_1, name, Constants.NAME, true, false, path)) {
      return false;
    }
    // M_5.12-1-1
//    if (!checkDuplicates.add(name)) {
//      setError(M_512_1_1, String.format("Check Constraint name %s must be unique (%s)", name, path));
//      return false;
//    }

    return true;
  }

  /**
   * M_5.12-1-3 The Check Constraint description in SIARD file must exist. ERROR
   * if not exist.
   *
   * @return true if valid otherwise false
   */
  private boolean validateCheckConstraintDescription(String description, String path) {
    return validateXMLField(M_512_1_3, description, Constants.DESCRIPTION, false, true, path);
  }
}
