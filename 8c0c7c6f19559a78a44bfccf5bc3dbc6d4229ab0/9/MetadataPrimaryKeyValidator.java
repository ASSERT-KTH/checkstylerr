package com.databasepreservation.modules.siard.validate.component.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

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
public class MetadataPrimaryKeyValidator extends MetadataValidator {
  private static final String MODULE_NAME = "Primary Key level metadata";
  private static final String M_58 = "5.8";
  private static final String M_581 = "M_5.8-1";
  private static final String M_581_1 = "M_5.8-1-1";
  private static final String M_581_2 = "M_5.8-1-2";
  private static final String M_581_3 = "M_5.8-1-3";
  private static final String BLANK = " ";

  private Map<String, LinkedList<String>> tableColumnsList = new HashMap<>();

  public static MetadataPrimaryKeyValidator newInstance() {
    return new MetadataPrimaryKeyValidator();
  }

  private MetadataPrimaryKeyValidator() {
    error.clear();
    warnings.clear();
  }

  @Override
  public boolean validate() throws ModuleException {
    if (preValidationRequirements())
      return false;

    getValidationReporter().moduleValidatorHeader(M_58, MODULE_NAME);

    if (!readXMLMetadataPrimaryKeyLevel()) {
      reportValidations(M_581, MODULE_NAME);
      closeZipFile();
      return false;
    }
    closeZipFile();

    if (reportValidations(M_581, MODULE_NAME) && reportValidations(M_581_1, MODULE_NAME)
      && reportValidations(M_581_2, MODULE_NAME) && reportValidations(M_581_3, MODULE_NAME)) {
      getValidationReporter().moduleValidatorFinished(MODULE_NAME, ValidationReporter.Status.PASSED);
      return true;
    }

    return false;
  }

  private boolean readXMLMetadataPrimaryKeyLevel() {
    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(getZipInputStream(validatorPathStrategy.getMetadataXMLPath()),
        "/ns:siardArchive/ns:schemas/ns:schema/ns:tables/ns:table", XPathConstants.NODESET,
        Constants.NAMESPACE_FOR_METADATA);

      for (int i = 0; i < nodes.getLength(); i++) {
        Element tableElement = (Element) nodes.item(i);
        String table = XMLUtils.getChildTextContext(tableElement, Constants.NAME);
        String tableFolder = XMLUtils.getChildTextContext(tableElement, Constants.FOLDER);
        String schema = XMLUtils.getChildTextContext((Element) tableElement.getParentNode().getParentNode(),
          Constants.NAME);
        String schemaFolder = XMLUtils
          .getChildTextContext((Element) tableElement.getParentNode().getParentNode(), Constants.FOLDER);

        Element tableColumnsElement = XMLUtils.getChild(tableElement, Constants.COLUMNS);
        if (tableColumnsElement == null) {
          return false;
        }
        NodeList tableColumns = tableColumnsElement.getElementsByTagName(Constants.COLUMN);

        LinkedList<String> tableColumnName = new LinkedList<>();
        for (int ci = 0; ci < tableColumns.getLength(); ci++) {
          tableColumnName.add(XMLUtils.getChildTextContext((Element) tableColumns.item(ci), Constants.NAME));
        }
        tableColumnsList.put(table, tableColumnName);

        NodeList primaryKeyNodes = tableElement.getElementsByTagName(Constants.PRIMARY_KEY);
        // primaryKeysCount.put(table, primaryKeyNodes.getLength());

        for (int j = 0; j < primaryKeyNodes.getLength(); j++) {
          Element primaryKey = (Element) primaryKeyNodes.item(j);

          String name = XMLUtils.getChildTextContext(primaryKey, Constants.NAME);

          // * M_5.8-1 Primary key name is mandatory.
          if (name == null || name.isEmpty()) {
            setError(M_581, "Primary key name is required on table " + table);
            return false;
          }

          // nameList.add(name);
          NodeList columns = primaryKey.getElementsByTagName(Constants.COLUMN);

          ArrayList<String> columnList = new ArrayList<>();
          for (int k = 0; k < columns.getLength(); k++) {
            String column = columns.item(k).getTextContent();
            // * M_5.8-1 Primary key column is mandatory.
            if (column == null || column.isEmpty()) {
              setError(M_581, "Primary key column is required on table " + table);
              return false;
            }
            columnList.add(column);
          }

          if (!validatePrimaryKeyName(schemaFolder, tableFolder, table, name, columnList))
            break;

          if (!validatePrimaryKeyColumn(schema, table, name, columnList))
            break;

          String description = XMLUtils.getChildTextContext(primaryKey, Constants.DESCRIPTION);
          String path = buildPath(Constants.SCHEMA, schema, Constants.TABLE, table, Constants.PRIMARY_KEY, name);
          if (!validatePrimaryKeyDescription(description, path))
            break;

        }

      }

    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      return false;
    }
    return true;
  }

  /**
   * M_5.8-1-1 The Primary Key name of table in SIARD file must be unique. ERROR
   * if not unique or is null. WARNING if contain any blanks
   *
   * @return true if valid otherwise false
   */
  private boolean validatePrimaryKeyName(String schemaFolder, String tableFolder, String table, String name,
    ArrayList<String> columnList) {

    List<String> columns = new ArrayList<>();
    for (String column : columnList) {
      if (tableColumnsList.get(table).indexOf(column) >= 0) {
        int columnIndex = tableColumnsList.get(table).indexOf(column) + 1;
        columns.add("c" + columnIndex);
      } else {
        setError(M_581_1, String.format("Column %s does not exist on table %s", column, table));
        return false;
      }
    }

    try {
      NodeList nodes = (NodeList) XMLUtils.getXPathResult(
        getZipInputStream(validatorPathStrategy.getXMLTablePathFromFolder(schemaFolder, tableFolder)),
        "/ns:table/ns:row", XPathConstants.NODESET, Constants.NAMESPACE_FOR_TABLE);

      Set<String> unique = new HashSet<>();

      for (int i = 0; i < nodes.getLength(); i++) {
        Element row = (Element) nodes.item(i);

        StringBuilder primaryColumn = new StringBuilder();
        for (int j = 0; j < columns.size(); j++) {
          String columnText = XMLUtils.getChildTextContext(row, columns.get(j));
          if (j > 0 && j < columns.size()) {
            primaryColumn.append(".");
          }
          primaryColumn.append(columnText);
        }

        if (!unique.add(primaryColumn.toString())) {
          setError(M_581_1, String.format("Found duplicates primary keys '%s' with value %s on %s.%s column %s", name,
            primaryColumn.toString(), schemaFolder, tableFolder, columns.toString()));
          return false;
        }
        ;
      }

    } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
      return false;
    }

    if (name.contains(BLANK)) {
      addWarning(M_581_1, "Primary key " + name + " contain blanks in name", name);
    }
    return true;
  }

  /**
   * M_5.8-1-2 The Primary Key column in SIARD file must exist on table. ERROR if
   * not exist
   *
   * @return true if valid otherwise false
   */
  private boolean validatePrimaryKeyColumn(String schema, String table, String name, List<String> columnList) {

    for (String column : columnList) {
      if (!tableColumnsList.get(table).contains(column)) {
        setError(M_581_2, String.format("Primary key column reference %s not found on %s.%s" + column, schema, table));
        return false;
      }
    }
    return true;
  }

  /**
   * M_5.8-1-3 The primary key description in SIARD file must not be less than 3
   * characters. WARNING if it is less than 3 characters
   */
  private boolean validatePrimaryKeyDescription(String description, String path) {
    return validateXMLField(M_581_3, description, Constants.DESCRIPTION, false, true, path);
  }
}