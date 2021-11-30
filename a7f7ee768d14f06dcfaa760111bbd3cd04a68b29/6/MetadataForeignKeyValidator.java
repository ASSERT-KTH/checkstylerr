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
import com.databasepreservation.utils.XMLUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataForeignKeyValidator extends MetadataValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataForeignKeyValidator.class);
  private final String MODULE_NAME;
  private static final String M_59 = "5.9";
  private static final String M_591 = "M_5.9-1";
  private static final String M_591_1 = "M_5.9-1-1";
  private static final String M_591_2 = "M_5.9-1-2";
  private static final String M_591_3 = "M_5.9-1-3";
  private static final String M_591_8 = "M_5.9-1-8";

  private List<Element> foreignKeyList = new ArrayList<>();
  private List<String> tableList = new ArrayList<>();
  private List<String> schemaList = new ArrayList<>();
  private Set<String> checkDuplicates = new HashSet<>();

  public MetadataForeignKeyValidator(String moduleName) {
    this.MODULE_NAME = moduleName;
    setCodeListToValidate(M_591, M_591_1, M_591_2, M_591_3, M_591_8);
  }

  @Override
  public boolean validate() throws ModuleException {
    observer.notifyStartValidationModule(MODULE_NAME, M_59);
    if (preValidationRequirements()) {
      LOGGER.debug("Failed to validate the pre-requirements for {}", MODULE_NAME);
      return false;
    }

    getValidationReporter().moduleValidatorHeader(M_59, MODULE_NAME);

    schemaList = getListOfSchemas();
    tableList = getListOfTables();

    if (!validateMandatoryXSDFields(M_591, FOREIGN_KEYS_TYPE,
      "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table/ns:foreignKeys/ns:foreignKey")) {
      reportValidations(M_591, MODULE_NAME);
      closeZipFile();
      return false;
    }

    if (!readXMLMetadataForeignKeyLevel()) {
      reportValidations(M_591, MODULE_NAME);
      closeZipFile();
      return false;
    }
    closeZipFile();

    if (foreignKeyList.isEmpty()) {
      getValidationReporter().skipValidation(M_591, "Database has no foreign keys");
      metadataValidationPassed(MODULE_NAME);
      return true;
    }

    if (reportValidations(MODULE_NAME)) {
      metadataValidationPassed(MODULE_NAME);
      return true;
    }
    return false;
  }

  private boolean readXMLMetadataForeignKeyLevel() {
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table", XPathConstants.NODESET,
        Constants.NAMESPACE_FOR_METADATA);

      for (int i = 0; i < nodes.getLength(); i++) {
        Element tableElement = (Element) nodes.item(i);
        String table = XMLUtils.getChildTextContext(tableElement, Constants.NAME);
        String schema = XMLUtils.getParentNameByTagName(tableElement, Constants.SCHEMA);

        NodeList foreignKeyNodes = tableElement.getElementsByTagName(Constants.FOREIGN_KEY);
        if (foreignKeyNodes == null) {
          // next table
          continue;
        }

        for (int j = 0; j < foreignKeyNodes.getLength(); j++) {
          Element foreignKey = (Element) foreignKeyNodes.item(j);
          foreignKeyList.add(foreignKey);

          String name = XMLUtils.getChildTextContext(foreignKey, Constants.NAME);
          String path = buildPath(Constants.SCHEMA, schema, Constants.TABLE, table, Constants.FOREIGN_KEY, name);
          // * M_5.9-1 Foreign key name is mandatory.
          if (name == null || name.isEmpty()) {
            setError(M_591, "Foreign key name is required on table " + table);
            return false;
          }
          if (!validateForeignKeyName(table, name))
            break;

          String referencedSchema = XMLUtils.getChildTextContext(foreignKey, Constants.FOREIGN_KEY_REFERENCED_SCHEMA);
          // * M_5.9-1 Foreign key referencedSchema is mandatory.
          if (referencedSchema == null || referencedSchema.isEmpty()) {
            setError(M_591, String.format("ReferencedSchema is mandatory (%s)", path));
            return false;
          }
          if (!validateForeignKeyReferencedSchema(referencedSchema, path))
            break;

          String referencedTable = XMLUtils.getChildTextContext(foreignKey, Constants.FOREIGN_KEY_REFERENCED_TABLE);
          // * M_5.9-1 Foreign key referencedTable is mandatory.
          if (referencedTable == null || referencedTable.isEmpty()) {
            setError(M_591, String.format("ReferencedTable is mandatory (%s)", path));
            return false;
          }
          if (!validateForeignKeyReferencedTable(referencedTable, name))
            break;

          String reference = XMLUtils.getChildTextContext(foreignKey, Constants.FOREIGN_KEY_REFERENCE);
          // * M_5.9-1 Foreign key referencedTable is mandatory.
          if (reference == null || reference.isEmpty()) {
            setError(M_591, String.format("Reference is mandatory (%s)", path));
            return false;
          }

          String description = XMLUtils.getChildTextContext(foreignKey, Constants.DESCRIPTION);
          if (!validateForeignKeyDescription(description, path))
            break;
        }
      }

    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      String errorMessage = "Unable to read foreign key from SIARD file";
      setError(M_591, errorMessage);
      LOGGER.debug(errorMessage, e);
      return false;
    }

    return true;
  }

  private List<String> getListOfSchemas() {
    List<String> schemaList = new ArrayList<>();
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema", XPathConstants.NODESET, Constants.NAMESPACE_FOR_METADATA);

      for (int i = 0; i < nodes.getLength(); i++) {
        Element schema = (Element) nodes.item(i);
        schemaList.add(XMLUtils.getChildTextContext(schema, "name"));
      }
    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      String errorMessage = "Unable to read schemas from SIARD file";
      LOGGER.debug(errorMessage, e);
    }
    return schemaList;
  }

  private List<String> getListOfTables() {
    List<String> tableList = new ArrayList<>();
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table", XPathConstants.NODESET,
        Constants.NAMESPACE_FOR_METADATA);

      for (int i = 0; i < nodes.getLength(); i++) {
        Element tables = (Element) nodes.item(i);
        tableList.add(XMLUtils.getChildTextContext(tables, Constants.NAME));
      }
    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      String errorMessage = "Unable to read tables from SIARD file";
      LOGGER.debug(errorMessage, e);
    }
    return tableList;
  }

  /**
   * M_5.9-1-1 The Foreign Key name in SIARD file must be unique. ERROR if not
   * unique
   *
   * @return true if valid otherwise false
   */
  private boolean validateForeignKeyName(String table, String name) {
    if (!checkDuplicates.add(name)) {
      setError(M_591_1, String.format("Foreign key name %s of table %s must be unique", name, table));
      return false;
    }

    return true;
  }

  /**
   * M_5.9-1-2 The Schema in referencedSchema must exist. ERROR if not exist
   *
   * @return true if valid otherwise false
   */
  private boolean validateForeignKeyReferencedSchema(String referencedSchema, String path) {
    if (!schemaList.contains(referencedSchema)) {
      setError(M_591_2, String.format("ReferencedSchema %s does not exist on database (%s)", referencedSchema, path));
      return false;
    }
    return true;
  }

  /**
   * M_5.9-1-3 The Table in referencedTable must exist. ERROR if not exist
   *
   * @return true if valid otherwise false
   */
  private boolean validateForeignKeyReferencedTable(String referencedTable, String path) {
    if (!tableList.contains(referencedTable)) {
      setError(M_591_3, String.format("ReferencedTable %s does not exist on database (%s)", referencedTable, path));
      return false;
    }

    return true;
  }

  /**
   * M_5.9-1-8 The foreign key description in SIARD file must not be less than 3
   * characters. WARNING if it is less than 3 characters
   */
  private boolean validateForeignKeyDescription(String description, String path) {
    return validateXMLField(M_591_8, description, Constants.DESCRIPTION, false, true, path);
  }
}
