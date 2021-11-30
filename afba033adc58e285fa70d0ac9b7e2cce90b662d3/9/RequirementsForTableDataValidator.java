package com.databasepreservation.modules.siard.validate.TableData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.modules.validate.ValidatorModule;
import com.databasepreservation.model.reporters.ValidationReporter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RequirementsForTableDataValidator extends ValidatorModule {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequirementsForTableDataValidator.class);

  private static final String MODULE_NAME = "Requirements for table data";
  private static final String P_60 = "T_6.0";
  private static final String P_601 = "T_6.0-1";
  private static final String P_602 = "T_6.0-2";
  private static final String XSD_EXTENSION = ".xsd";
  private static final String XML_EXTENSION = ".xml";

  public static RequirementsForTableDataValidator newInstance() {
    return new RequirementsForTableDataValidator();
  }

  private RequirementsForTableDataValidator() {
  }

  @Override
  public boolean validate() throws ModuleException {
    if (preValidationRequirements())
      return false;

    getValidationReporter().moduleValidatorHeader(P_60, MODULE_NAME);

    if (validateTableXSDAgainstXML()) {
      getValidationReporter().validationStatus(P_602, ValidationReporter.Status.OK);
    } else {
      validationFailed(P_602, MODULE_NAME);
      closeZipFile();
      return false;
    }

    getValidationReporter().moduleValidatorFinished(MODULE_NAME, ValidationReporter.Status.OK);
    closeZipFile();

    return true;
  }

  /**
   * T_6.0-1
   *
   * The schema definition table[number].xsd must be complied with for the
   * table[number].xml file. This means that table[number].xml must be capable of
   * being positively validated against table[number].xsd.
   * 
   * @return true if valid otherwise false
   */
  private boolean validateTableXSDAgainstXML() {
    if (preValidationRequirements())
      return false;

    Set<String> tableDataSchemaDefinition = new HashSet<>();

    for (String path : getZipFileNames()) {
      String regexPattern = "^(content/schema[0-9]+/table[0-9]+/table[0-9]+)\\.(xsd|xml)$";

      Pattern pattern = Pattern.compile(regexPattern);
      Matcher matcher = pattern.matcher(path);

      while (matcher.find()) {
        tableDataSchemaDefinition.add(matcher.group(1));
      }
    }

    for (String path : tableDataSchemaDefinition) {
      String XSDPath = path.concat(XSD_EXTENSION);
      String XMLPath = path.concat(XML_EXTENSION);

      final ZipArchiveEntry XSDEntry = getZipFile().getEntry(XSDPath);
      final ZipArchiveEntry XMLEntry = getZipFile().getEntry(XMLPath);
      InputStream XSDInputStream;
      InputStream XMLInputStream;
      try {
        XSDInputStream = getZipFile().getInputStream(XSDEntry);
        XMLInputStream = getZipFile().getInputStream(XMLEntry);
      } catch (IOException e) {
        return false;
      }

      Source schemaFile = new StreamSource(XSDInputStream);
      Source xmlFile = new StreamSource(XMLInputStream);

      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema;
      try {
        schema = schemaFactory.newSchema(schemaFile);
      } catch (SAXException e) {
        return false;
        // System.out.println("Reason: " + e.getLocalizedMessage());
      }

      Validator validator = schema.newValidator();
      try {
        validator.validate(xmlFile);
      } catch (SAXException | IOException e) {
        // System.out.println("Reason: " + e.getLocalizedMessage());
        return false;
      }
    }

    return true;
  }
}
