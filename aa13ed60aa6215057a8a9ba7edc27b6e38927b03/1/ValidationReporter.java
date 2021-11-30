package com.databasepreservation.model.reporters;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.Constants;
import com.databasepreservation.utils.MiscUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ValidationReporter implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationReporter.class);
  private Path outputFile;
  private BufferedWriter writer;

  public enum Status {
    OK, ERROR, WARNING, SKIPPED, NOTICE, PASSED, FAILED, START, FINISH;
  }

  public enum Indent {
    TAB, TAB_2, TAB_4
  }

  private static final String EMPTY_MESSAGE_LINE = "";
  private static final String NEWLINE = System.getProperty("line.separator", "\n");
  private static final String TAB = "\t";
  private static final String TAB_2 = "\t\t";
  private static final String TAB_4 = "\t\t\t";
  private static final String COLON = ":";
  private static final String SINGLE_SPACE = " ";
  private static final String OPEN_BRACKET = "[";
  private static final String OPEN_PARENTHESES = "(";
  private static final String CLOSED_BRACKET = "]";
  private static final String CLOSED_PARENTHESES = ")";
  private static final String HYPHEN = "-";

  public ValidationReporter(Path path, Path SIARDPackagePath) {
    init(path, SIARDPackagePath);
  }

  private void init(Path path, Path SIARDPackagePath) {
    this.outputFile = path;
    if (Files.notExists(outputFile)) {
      try {
        Files.createFile(outputFile);
      } catch (IOException e) {
        LOGGER.warn("Could not create report file in current working directory. Attempting to use a temporary file", e);
        try {
          outputFile = Files.createTempFile(Constants.DBPTK_VALIDATION_REPORTER_PREFIX, ".txt");
        } catch (IOException e1) {
          LOGGER.error("Could not create report temporary file. Reporting will not function.", e1);
        }
      }
    }

    if (outputFile != null) {
      try {
        writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
        writeLine("######################################################################################################################################");
        writeLine("#                                                       DBPTK - Validation Report                                                    #");
        writeLine("######################################################################################################################################");
        writeLine("DBPTK Version: " + MiscUtils.APP_NAME_AND_VERSION);
        writeLine("Date: " + new org.joda.time.DateTime());
        writeLine("SIARD file: " + SIARDPackagePath.toAbsolutePath().normalize().toString());
        writeLine(NEWLINE);
      } catch (IOException e) {
        LOGGER.error("Could not get a writer for the report file.", e);
      }
    }
  }

  public void moduleValidatorHeader(String text) {
    writeLine(text);
  }

  public void moduleValidatorHeader(String ID, String text) {
    writeLine(ID + SINGLE_SPACE + HYPHEN + SINGLE_SPACE + text);
  }

  public void validationStatus(String text, Status status) {
    writeLine(TAB + text + COLON + SINGLE_SPACE + buildStatus(status));
  }

  public void validationStatus(String text, Status status, String details, Indent indent) {
    writeLine(resolveIndent(indent) + text + COLON + SINGLE_SPACE + buildStatus(status) + SINGLE_SPACE + details);
  }

  public void validationStatus(Status status, String details, Indent indent) {
    writeLine(resolveIndent(indent) + buildStatus(status) + SINGLE_SPACE + details);
  }

  public void validationStatus(String text, Status status, String details) {
    writeLine(TAB + text + COLON + SINGLE_SPACE + buildStatus(status) + SINGLE_SPACE + details);
  }

  public void validationStatus(String text, Status status, String pathToEntry, List<String> details) {
    writeLine(TAB + text + COLON + SINGLE_SPACE + buildStatus(status));
    for (String detail: details ) {
      writeLine(TAB  + TAB +  pathToEntry + COLON +SINGLE_SPACE + detail);
    }
  }

  public void validationStatus(String text, Status status, String pathToEntry, Map<String, List<String>> details) {
    writeLine(TAB + text + COLON + SINGLE_SPACE + buildStatus(status));
    for (Map.Entry<String, List<String>> entry: details.entrySet() ) {
      for(String detail: entry.getValue()){
        writeLine(TAB  + TAB +  pathToEntry + COLON +SINGLE_SPACE + detail);
      }
    }
  }

  public void moduleValidatorFinished(String text, Status status) {
    writeLine(text + SINGLE_SPACE + buildStatus(status));
    writeLine(EMPTY_MESSAGE_LINE);
  }

  public void warning(String ID, String text, String object) {
    writeLine(TAB + ID + COLON + SINGLE_SPACE + buildStatus(Status.WARNING) + SINGLE_SPACE + HYPHEN + SINGLE_SPACE
      + text + SINGLE_SPACE + OPEN_PARENTHESES + object + CLOSED_PARENTHESES);
  }

  public void skipValidation(String ID, String reasonToSkip) {
    writeLine(TAB + ID + COLON + SINGLE_SPACE + buildStatus(Status.SKIPPED) + SINGLE_SPACE + HYPHEN + SINGLE_SPACE
      + reasonToSkip);
  }

  public void notice(Object nodeValue, String noticeMessage) {
    writeLine(TAB + buildStatus(Status.NOTICE) + COLON + SINGLE_SPACE + noticeMessage + COLON + SINGLE_SPACE + nodeValue);
  }

  private String buildStatus(Status status) {
    return OPEN_BRACKET + status.name() + CLOSED_BRACKET;
  }

  private void writeLine(String line) {
    if (outputFile == null || writer == null) {
      LOGGER.info(line);
    } else {
      try {
        writer.write(line);
        writer.newLine();
      } catch (IOException e) {
        LOGGER.trace("IOException when trying to write report line", e);
        if (e.getMessage().equals("Stream closed")) {
          writer = null;
        }
        LOGGER.info(line);
      }
    }
  }

  private String resolveIndent(Indent indent) {
    switch (indent) {
      case TAB_2:
        return TAB_2;
      case TAB_4:
        return TAB_4;
      default:
        return TAB;
    }
  }

  @Override
  public void close() {
    try {
      if (writer != null) {
        writer.close();
      }
    } catch (IOException e) {
      LOGGER.debug("Unable to close validation reporter file", e);
    } finally {
      if (writer != null) {
        LOGGER.info("A report was generated with a listing of information about the individual validations.");
        LOGGER.info("The report file is located at {}", outputFile.normalize().toAbsolutePath().toString());
      } else {
        LOGGER.info(
          "A report with a listing of information  about the individual validations could not be generated, please submit a bug report to help us fix this.");
      }
    }
  }
}