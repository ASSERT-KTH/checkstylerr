package com.databasepreservation.modules.siard.constants;

import java.io.File;

/**
 * @author Andreas Kring <andreas@magenta.dk>
 *
 */
public class SIARDDKConstants {

  // System dependent file seperator etc. ("/" on Linux and "\" on Windows)
  public static final String FILE_SEPARATOR = File.separator;
  public static final String RESOURCE_FILE_SEPARATOR = "/";
  public static final String FILE_EXTENSION_SEPARATOR = ".";

  // File extensions
  public static final String XML_EXTENSION = "xml";
  public static final String XSD_EXTENSION = "xsd";
  public static final String UNKNOWN_MIMETYPE_BLOB_EXTENSION = "bin";

  // Name of the context documentation folder within the archive
  public static final String CONTEXT_DOCUMENTATION_RELATIVE_PATH = "ContextDocumentation";

  // Path to schemas in the /src/main/resources folder
  public static final String SCHEMA_RESOURCE_DIR = "schema";

  // JAXB context for tableIndex, fileIndex and docIndex
  public static final String JAXB_CONTEXT_TABLEINDEX = "dk.sa.xmlns.diark._1_0.tableindex";
  public static final String JAXB_CONTEXT_FILEINDEX = "dk.sa.xmlns.diark._1_0.fileindex";
  public static final String JAXB_CONTEXT_DOCINDEX = "dk.sa.xmlns.diark._1_0.docindex";

  // Key for context documentation folder (given on command line)
  public static final String CONTEXT_DOCUMENTATION_FOLDER = "contextDocumentationFolder";

  // Keys used in the metadata contexts
  public static final String CONTEXT_DOCUMENTATION_INDEX = "contextDocumentationIndex";
  public static final String ARCHIVE_INDEX = "archiveIndex";
  public static final String TABLE_INDEX = "tableIndex";
  public static final String FILE_INDEX = "fileIndex";
  public static final String DOC_INDEX = "docIndex";
  public static final String DOCUMENT_IDENTIFICATION = "documentIdentification";
  public static final String XML_SCHEMA = "XMLSchema";

  // Constants for LOBs
  public static final String BINARY_LARGE_OBJECT = "BINARY LARGE OBJECT";
  public static final String CHARACTER_LARGE_OBJECT = "CHARACTER LARGE OBJECT";
  public static final String DEFAULT_MAX_CLOB_LENGTH = "2048";
  public static final String DEFAULT_CLOB_TYPE = "NATIONAL CHARACTER VARYING";

  // Namespaces
  public static final String DBPTK_NS = "http://www.databasepreservation.com/xmlns/1.0";

  // Digest algorithms
  public static final String DIGEST_ALGORITHM = "MD5";
}
