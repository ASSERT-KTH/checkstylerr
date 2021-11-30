package com.databasepreservation.modules.siard.validate.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataTypeValidator extends MetadataValidator {
  private static final String MODULE_NAME = "Type level metadata";
  private static final String M_53 = "5.3";
  private static final String M_531 = "M_5.3-1";
  private static final String M_5311 = "M_5.3-1-1";
  private static final String M_5312 = "M_5.3-1-2";
  private static final String M_5315 = "M_5.3-1-5";
  private static final String M_5316 = "M_5.3-1-6";
  private static final String M_53110 = "M_5.3-1-10";

  private List<Element> typesList = new ArrayList<>();
  private List<String> nameList = new ArrayList<>();
  private List<String> categoryList = new ArrayList<>();
  private List<String> instantiableList = new ArrayList<>();
  private List<String> finalList = new ArrayList<>();
  private List<String> descriptionList = new ArrayList<>();

  public static MetadataTypeValidator newInstance() {
    return new MetadataTypeValidator();
  }

  private MetadataTypeValidator() {
  }

  @Override
  public boolean validate() {
    getValidationReporter().moduleValidatorHeader(M_53, MODULE_NAME);

    if (!reportValidations(readXMLMetadataTypeLevel(), M_531, true)) {
      return false;
    }

    // there is no need to continue the validation if no have types in any schema
    if (typesList.isEmpty()) {
      return true;
    }

    if (!reportValidations(validateTypeName(), M_5311, true)) {
      return false;
    }

    if (!reportValidations(validateTypeCategory(), M_5312, true)) {
      return false;
    }

    if (!reportValidations(validateTypeInstantiable(), M_5315, true)) {
      return false;
    }

    if (!reportValidations(validateTypefinal(), M_5316, true)) {
      return false;
    }

    reportValidations(validateTypeDescription(), M_53110, false);

    return true;
  }

  private boolean readXMLMetadataTypeLevel() {
    try (ZipFile zipFile = new ZipFile(getSIARDPackagePath().toFile())) {
      final ZipArchiveEntry metadataEntry = zipFile.getEntry("header/metadata.xml");
      final InputStream inputStream = zipFile.getInputStream(metadataEntry);
      Document document = MetadataXMLUtils.getDocument(inputStream);
      String xpathExpressionDatabase = "/ns:siardArchive/ns:schemas/ns:schema/ns:types/ns:type";

      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xpath = xPathFactory.newXPath();

      xpath = MetadataXMLUtils.setXPath(xpath, null);

      try {
        XPathExpression expr = xpath.compile(xpathExpressionDatabase);
        NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
          Element type = (Element) nodes.item(i);
          typesList.add(type);

          Element nameElement = MetadataXMLUtils.getChild(type, "name");
          String name = nameElement != null ? nameElement.getTextContent() : null;
          nameList.add(name);

          Element categoryElement = MetadataXMLUtils.getChild(type, "category");
          String category = categoryElement != null ? categoryElement.getTextContent() : null;
          categoryList.add(category);

          Element instantiableElement = MetadataXMLUtils.getChild(type, "instantiable");
          String instantiable = instantiableElement != null ? instantiableElement.getTextContent() : null;
          instantiableList.add(instantiable);

          Element finalElement = MetadataXMLUtils.getChild(type, "final");
          String finalField = finalElement != null ? finalElement.getTextContent() : null;
          finalList.add(finalField);

          Element descriptionElement = MetadataXMLUtils.getChild(type, "description");
          String description = descriptionElement != null ? descriptionElement.getTextContent() : null;
          descriptionList.add(description);

        }

      } catch (XPathExpressionException e) {
        return false;
      }

    } catch (IOException | ParserConfigurationException | SAXException e) {
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
  private boolean validateTypeName() {
    return validateMandatoryXMLFieldList(nameList, "name", false);
  }

  /**
   * M_5.3-1-2 The type category in the schema must not be empty. ERROR when it is
   * empty
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypeCategory() {
    return validateMandatoryXMLFieldList(categoryList, "category", false);
  }

  /**
   * M_5.3-1-5 The type instantiable field in the schema must not be empty. ERROR
   * when it is empty
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypeInstantiable() {
    return validateMandatoryXMLFieldList(instantiableList, "instantiable", false);
  }

  /**
   * M_5.3-1-6 The type final field in the schema must not be empty. ERROR when it
   * is empty
   *
   * @return true if valid otherwise false
   */
  private boolean validateTypefinal() {
    return validateMandatoryXMLFieldList(finalList, "final", false);
  }

  /**
   * M_5.3-1-10 The type description field in the schema must not be must not be
   * less than 3 characters. WARNING if it is less than 3 characters
   *
   */
  private boolean validateTypeDescription() {
    validateXMLFieldSizeList(descriptionList, "description");
    return true;
  }

}
