package com.databasepreservation.modules.siard.validate.component.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.databasepreservation.Constants;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.reporters.ValidationReporter;
import com.databasepreservation.modules.siard.constants.SIARDDKConstants;
import com.databasepreservation.utils.XMLUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataFieldValidator extends MetadataValidator {

  private static final String MODULE_NAME = "Field level metadata";
  private static final String M_57 = "5.7";
  private static final String M_571 = "M_5.7-1";
  private static final String M_571_1 = "M_5.7-1-1";
  private static final String M_571_2 = "M_5.7-1-2";
  private static final String M_571_5 = "M_5.7-1-5";

  private static final String ARRAY = "ARRAY";

  private List<Element> fieldList = new ArrayList<>();

  public static MetadataValidator newInstance() {
    return new MetadataFieldValidator();
  }

  private MetadataFieldValidator() {
    error.clear();
    warnings.clear();
  }

  @Override
  public boolean validate() throws ModuleException {
    if (preValidationRequirements())
      return false;

    getValidationReporter().moduleValidatorHeader(M_57, MODULE_NAME);

    if (readXMLMetadataFieldLevel()) {
      closeZipFile();
      if (fieldList.isEmpty()) {
        getValidationReporter().skipValidation(M_571, "Database has no fields");
        getValidationReporter().moduleValidatorFinished(MODULE_NAME, ValidationReporter.Status.PASSED);
        return true;
      }
    }
    closeZipFile();

    if (reportValidations(M_571, MODULE_NAME) && reportValidations(M_571_1, MODULE_NAME)
      && reportValidations(M_571_2, MODULE_NAME) && reportValidations(M_571_5, MODULE_NAME)) {
      getValidationReporter().moduleValidatorFinished(MODULE_NAME, ValidationReporter.Status.PASSED);
      return true;
    }

    return false;
  }

  private boolean readXMLMetadataFieldLevel() {
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table/ns:columns/ns:column", XPathConstants.NODESET,
        Constants.NAMESPACE_FOR_METADATA);

      if (nodes == null) {
        return true;
      }

      for (int i = 0; i < nodes.getLength(); i++) {
        Element column = (Element) nodes.item(i);
        String columnName = MetadataXMLUtils.getChildTextContext(column, Constants.NAME);
        String columnTypeName = MetadataXMLUtils.getChildTextContext(column, Constants.TYPE_NAME);
        String columnTypeSchema = MetadataXMLUtils.getChildTextContext(column, Constants.TYPE_SCHEMA);
        String columnTypeOriginal = MetadataXMLUtils.getChildTextContext(column, Constants.TYPE_ORIGINAL);
        String schemaName = MetadataXMLUtils.getParentNameByTagName(column, Constants.SCHEMA);
        String tableName = MetadataXMLUtils.getParentNameByTagName(column, Constants.TABLE);

        Element fieldsElement = MetadataXMLUtils.getChild(column, Constants.FIELDS);
        if (fieldsElement == null) {
          // next schema
          continue;
        }
        fieldList.add(fieldsElement);

        String xpathField = String.format(
          "/ns:siardArchive/ns:schemas/ns:schema[ns:name='%s']/ns:tables/ns:table[ns:name='%s']/ns:columns/ns:column[ns:name='%s']/ns:fields/ns:field",
          schemaName, tableName, columnName);
        NodeList fieldNodes = (NodeList) XMLUtils.getXPathResult(
          getZipInputStream(validatorPathStrategy.getMetadataXMLPath()), xpathField, XPathConstants.NODESET,
          Constants.NAMESPACE_FOR_METADATA);

        for (int j = 0; j < fieldNodes.getLength(); j++) {

          Element field = (Element) fieldNodes.item(j);
          String name = MetadataXMLUtils.getChildTextContext(field, Constants.NAME);
          String path = buildPath(Constants.SCHEMA, schemaName, Constants.TABLE, tableName, Constants.COLUMN,
            columnName, Constants.FIELD, name);

          if (!validateFieldName(name, path)) {
            return false;
          }

          String lobFolder = MetadataXMLUtils.getChildTextContext(field, Constants.LOB_FOLDER);
          if (!validateType(columnTypeName, columnTypeSchema, columnTypeOriginal, name, lobFolder, path)) {
            return false;
          }

          String description = MetadataXMLUtils.getChildTextContext(field, Constants.DESCRIPTION);
          validateFieldDescription(description, path);
        }
      }

    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      setError(M_571, "Cannot read fields from SIARD");
      return false;
    }
    return true;
  }

  // Using the attribute found through column typeName and typeSchema to validate
  // category and lobFolder
  private boolean validateType(String columnTypeName, String columnTypeSchema, String columnTypeOriginal,
    String fieldName, String lobFolder, String path) {
    if (columnTypeOriginal != null && columnTypeOriginal.contains(ARRAY)) {
      return true;
    }
    try {
      String xPathExpression = String.format(
        "/ns:siardArchive/ns:schemas/ns:schema[ns:name='%s']/ns:types/ns:type[ns:name='%s' and ns:attributes/ns:attribute[ns:name='%s']]",
        columnTypeSchema, columnTypeName, fieldName);

      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        xPathExpression, XPathConstants.NODESET, Constants.NAMESPACE_FOR_METADATA);

      if (nodes.getLength() < 1) {
        setError(M_571,
          String.format("Field '%s' does not match with any attributes in the database (%s)", fieldName, path));
        return false;
      }

      for (int i = 0; i < nodes.getLength(); i++) {
        Element typeElement = (Element) nodes.item(i);
        String category = MetadataXMLUtils.getChildTextContext(typeElement, Constants.CATEGORY);

        if (!validateTypeCategory(category, columnTypeName, path)) {
          return false;
        }

        xPathExpression = String.format(
          "/ns:siardArchive/ns:schemas/ns:schema[ns:name='%s']/ns:types/ns:type[ns:name='%s']/ns:attributes/ns:attribute[ns:name='%s']",
          columnTypeSchema, columnTypeName, fieldName);

        NodeList attributeNode = (NodeList) XMLUtils.getXPathResult(
          getZipInputStream(validatorPathStrategy.getMetadataXMLPath()), xPathExpression, XPathConstants.NODESET,
          Constants.NAMESPACE_FOR_METADATA);

        for (int k = 0; k < attributeNode.getLength(); k++) {
          Element attribute = (Element) attributeNode.item(k);
          String attributeType = MetadataXMLUtils.getChildTextContext(attribute, Constants.TYPE);
          String attributeTypeName = MetadataXMLUtils.getChildTextContext(attribute, Constants.TYPE_NAME);

          if (!validateFieldLobFolder(lobFolder, attributeType, attributeTypeName, path)) {
            return false;
          }
        }
      }
    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      setError(M_571, "Cannot read attributes from SIARD");
      return false;
    }
    return true;
  }

  /**
   * M_5.7-1 The following field metadata are stored in the metadata.xml file if a
   * column or a field is an advanced or structured data type of category “distinct” or
   * “udt”:
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypeCategory(String category, String columnTypeName, String path) {
    if (columnTypeName != null) {
      if (category == null) {
        setError(M_571, String.format("Type '%s' does not exist on SIARD file (%s)", columnTypeName, path));
        return false;
      } else if (!category.equals(Constants.DISTINCT) && !category.equals(Constants.UDT)) {
        setError(M_571, String.format("Category of type '%s' must be DISTINCT or UDT (%s)", columnTypeName, path));
        return false;
      }
    } else {
      setError(M_571, String.format("Column typeName must exist (%s)", path));
      return false;
    }
    return true;
  }

  /**
   * M_5.7-1-1 The field name is mandatory.(SIARD Format Specification)
   *
   * @return true if valid otherwise false
   */
  private boolean validateFieldName(String name, String path) {
    return validateXMLField(M_571_1, name, Constants.NAME, true, false, path);
  }

  /**
   * M_5.7-1-2 The field name is mandatory.(SIARD Format Specification)
   *
   * @return true if valid otherwise false
   */
  private boolean validateFieldLobFolder(String lobFolder, String attributeType, String attributeTypeName,
    String path) {
    String type = attributeType != null ? attributeType : attributeTypeName;

    if (type == null) {
      setError(M_571_2, String.format("Attribute does not have a type or typeOriginal (%s)", path));
      return false;
    }

    if (!type.equals(Constants.CHARACTER_LARGE_OBJECT) && !type.equals(Constants.BINARY_LARGE_OBJECT)
      && !type.equals(Constants.BLOB) && !type.equals(Constants.CLOB) && !type.equals(Constants.XML_LARGE_OBJECT)) {
      return true;
    }

    return validateXMLField(M_571_2, lobFolder, Constants.LOB_FOLDER, true, false, path);
  }

  /**
   * M_5.7-1-5 The field description in SIARD file must not be empty.
   *
   * @return true if valid otherwise false
   */
  private boolean validateFieldDescription(String description, String path) {
    return validateXMLField(M_571_5, description, Constants.DESCRIPTION, false, true, path);
  }

}
