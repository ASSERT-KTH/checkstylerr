/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.cli;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.Constants;
import com.databasepreservation.model.exception.LicenseNotAcceptedException;
import com.databasepreservation.model.exception.UnreachableException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.ParameterGroup;
import com.databasepreservation.model.parameters.Parameters;
import com.databasepreservation.utils.MiscUtils;

/**
 * Handles command line interface.
 * 
 * Uses lazy parsing of parameters. Which means that the parameters are parsed
 * implicitly when something is requested that required them to be processed
 * (example: get the specified import or export modules).
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class CLI {
  private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);

  private final ArrayList<DatabaseModuleFactory> factories;
  private final List<String> commandLineArguments;

  private String importModuleName;
  private DatabaseModuleFactory importModuleFactory;
  private Map<Parameter, String> importModuleParameters;

  private String exportModuleName;
  private DatabaseModuleFactory exportModuleFactory;
  private Map<Parameter, String> exportModuleParameters;

  private boolean forceDisableEncryption = false;

  /**
   * Create a new CLI handler
   *
   * @param commandLineArguments
   *          List of command line parameters as they are received by Main.main
   * @param databaseModuleFactories
   *          List of available module factories
   */
  public CLI(List<String> commandLineArguments, Collection<DatabaseModuleFactory> databaseModuleFactories) {
    factories = new ArrayList<>(databaseModuleFactories);
    this.commandLineArguments = sanitizeCommandLineArguments(commandLineArguments);
  }

  /**
   * Create a new CLI handler
   *
   * @param commandLineArguments
   *          List of command line parameters as they are received by Main.main
   * @param databaseModuleFactories
   *          Array of module factories
   */
  public CLI(List<String> commandLineArguments, DatabaseModuleFactory... databaseModuleFactories) {
    this(commandLineArguments, Arrays.asList(databaseModuleFactories));
  }

  /**
   * Replaces dashes (https://en.wikipedia.org/wiki/Dash#Common_dashes) with the
   * only supported "dash" character: '-'
   */
  private List<String> sanitizeCommandLineArguments(List<String> commandLineArguments) {
    List<String> result = new ArrayList<>(commandLineArguments.size());
    Pattern pattern = Pattern.compile("^[\\u2012-\\u2015]([\\u2012-\\u2015])?");
    for (String commandLineArgument : commandLineArguments) {
      result.add(pattern.matcher(commandLineArgument).replaceAll("--"));
    }
    return result;
  }

  /**
   * Gets the database import module parameters, obtained by parsing the
   * parameters
   *
   * @return The import module configuration parameters
   * @throws ParseException
   *           if there was an error parsing the command line parameters
   * @throws LicenseNotAcceptedException
   *           if the license for using a module was not accepted
   */
  public Map<Parameter, String> getImportModuleParameters() throws ParseException, LicenseNotAcceptedException {
    if (importModuleFactory == null) {
      parse(commandLineArguments);
    }
    return importModuleParameters;
  }

  /**
   * Gets the database import module factory, obtained by parsing the parameters
   *
   * @return The database module factory capable of producing the import module
   *         specified in the parameters
   * @throws ParseException
   *           if there was an error parsing the command line parameters
   * @throws LicenseNotAcceptedException
   *           if the license for using a module was not accepted
   */
  public DatabaseModuleFactory getImportModuleFactory() throws ParseException, LicenseNotAcceptedException {
    if (importModuleFactory == null) {
      parse(commandLineArguments);
    }
    return importModuleFactory;
  }

  /**
   * Gets the database export module parameters, obtained by parsing the
   * parameters
   *
   * @return The export module configuration parameters
   * @throws ParseException
   *           if there was an error parsing the command line parameters
   * @throws LicenseNotAcceptedException
   *           if the license for using a module was not accepted
   */
  public Map<Parameter, String> getExportModuleParameters() throws ParseException, LicenseNotAcceptedException {
    if (exportModuleFactory == null) {
      parse(commandLineArguments);
    }
    return exportModuleParameters;
  }

  /**
   * Gets the database export module factory, obtained by parsing the parameters
   *
   * @return The database module factory capable of producing the export module
   *         specified in the parameters
   * @throws ParseException
   *           if there was an error parsing the command line parameters
   * @throws LicenseNotAcceptedException
   *           if the license for using a module was not accepted
   */
  public DatabaseModuleFactory getExportModuleFactory() throws ParseException, LicenseNotAcceptedException {
    if (exportModuleFactory == null) {
      parse(commandLineArguments);
    }
    return exportModuleFactory;
  }

  /**
   * Gets the name of the export module. Note that this method does not trigger
   * the lazy loading mechanism for parsing the parameters, so the value may be
   * null if no calls to getImportModule() or getExportModule() were made.
   *
   * @return The export module name. null if the command line parameters have not
   *         been parsed yet
   */
  public String getExportModuleName() {
    return exportModuleName;
  }

  /**
   * Gets the name of the import module. Note that this method does not trigger
   * the lazy loading mechanism for parsing the parameters, so the value may be
   * null if no calls to getImportModule() or getExportModule() were made.
   *
   * @return The import module name. null if the command line parameters have not
   *         been parsed yet
   */
  public String getImportModuleName() {
    return importModuleName;
  }

  /**
   * Outputs the help text to STDOUT
   */
  public void printHelp() {
    printHelp(System.out);
  }

  /**
   * Discards import and export module instances and disables encryption. Next
   * time #parse is run, encryption will be disabled for modules that support that
   * option.
   */
  public void disableEncryption() {
    forceDisableEncryption = true;
    importModuleFactory = null;
    exportModuleFactory = null;
  }

  /**
   * Parses the argument list and creates new import and export modules
   *
   * @param args
   *          The command line arguments
   * @throws ParseException
   *           If the arguments could not be parsed or are invalid
   */
  private void parse(List<String> args) throws ParseException, LicenseNotAcceptedException {
    DatabaseModuleFactoriesPair databaseModuleFactoriesPair = getModuleFactories(args);

    importModuleFactory = databaseModuleFactoriesPair.getImportModuleFactory();
    exportModuleFactory = databaseModuleFactoriesPair.getExportModuleFactory();

    try {
      importModuleName = importModuleFactory.getModuleName();
      exportModuleName = exportModuleFactory.getModuleName();
      DatabaseModuleFactoriesArguments databaseModuleFactoriesArguments = getModuleArguments(
        databaseModuleFactoriesPair, args);

      if (forceDisableEncryption) {
        // inject disable encryption for import module
        for (Parameter parameter : importModuleFactory.getImportModuleParameters().getParameters()) {
          if (parameter.longName().equalsIgnoreCase("disable-encryption")) {
            if (!databaseModuleFactoriesArguments.getImportModuleArguments().containsKey(parameter)) {
              databaseModuleFactoriesArguments.getImportModuleArguments().put(parameter, "true");
            }
            break;
          }
        }

        // inject disable encryption for export module
        for (Parameter parameter : exportModuleFactory.getExportModuleParameters().getParameters()) {
          if (parameter.longName().equalsIgnoreCase("disable-encryption")) {
            if (!databaseModuleFactoriesArguments.getExportModuleArguments().containsKey(parameter)) {
              databaseModuleFactoriesArguments.getExportModuleArguments().put(parameter, "true");
            }
            break;
          }
        }
      }

      importModuleParameters = databaseModuleFactoriesArguments.getImportModuleArguments();
      exportModuleParameters = databaseModuleFactoriesArguments.getExportModuleArguments();
    } catch (UnsupportedModuleException e) {
      LOGGER.debug("UnsupportedModuleException", e);
      throw new ParseException("Module does not support the requested mode.");
    }
  }

  /**
   * Given the arguments, determines the DatabaseModuleFactory objects that should
   * be used to create the import and export modules
   *
   * @param args
   *          The command line arguments
   * @return A pair of DatabaseModuleFactory objects containing the selected
   *         import and export module factories
   * @throws ParseException
   *           If the arguments could not be parsed or are invalid
   */
  private DatabaseModuleFactoriesPair getModuleFactories(List<String> args) throws ParseException {
    // check if args contains exactly one import and one export module
    String importModuleName = null;
    String exportModuleName = null;
    int importModulesFound = 0;
    int exportModulesFound = 0;
    Iterator<String> argsIterator = args.iterator();
    try {
      while (argsIterator.hasNext()) {
        String arg = argsIterator.next();
        if ("-i".equals(arg) || "--import".equals(arg)) {
          importModuleName = argsIterator.next();
          importModulesFound++;
        } else if ("-e".equals(arg) || "--export".equals(arg)) {
          exportModuleName = argsIterator.next();
          exportModulesFound++;
        } else if (StringUtils.startsWith(arg, "--import=")) {
          // 9 is the size of the string "--import="
          importModuleName = arg.substring(9);
          importModulesFound++;
        } else if (StringUtils.startsWith(arg, "--export=")) {
          // 9 is the size of the string "--export="
          exportModuleName = arg.substring(9);
          exportModulesFound++;
        }
      }
    } catch (NoSuchElementException e) {
      LOGGER.debug("NoSuchElementException", e);
      throw new ParseException("Missing module name.");
    }

    if (importModulesFound != 1 || exportModulesFound != 1) {
      throw new ParseException("Exactly one import module and one export module must be specified.");
    }

    // check if both module names correspond to real module names
    DatabaseModuleFactory importModuleFactory = null;
    DatabaseModuleFactory exportModuleFactory = null;
    for (DatabaseModuleFactory factory : factories) {
      String moduleName = factory.getModuleName();
      if (moduleName.equalsIgnoreCase(importModuleName) && factory.producesImportModules()) {
        importModuleFactory = factory;
      }
      if (moduleName.equalsIgnoreCase(exportModuleName) && factory.producesExportModules()) {
        exportModuleFactory = factory;
      }
    }
    if (importModuleFactory == null) {
      throw new ParseException("Invalid import module.");
    } else if (exportModuleFactory == null) {
      throw new ParseException("Invalid export module.");
    }
    return new DatabaseModuleFactoriesPair(importModuleFactory, exportModuleFactory);
  }

  /**
   * Obtains the arguments needed to create new import and export modules
   *
   * @param factoriesPair
   *          A pair of DatabaseModuleFactory objects containing the selected
   *          import and export module factories
   * @param args
   *          The command line arguments
   * @return A DatabaseModuleFactoriesArguments containing the arguments to create
   *         the import and export modules
   * @throws ParseException
   *           If the arguments could not be parsed or are invalid
   */
  private DatabaseModuleFactoriesArguments getModuleArguments(DatabaseModuleFactoriesPair factoriesPair,
    List<String> args) throws ParseException, UnsupportedModuleException {
    DatabaseModuleFactory importModuleFactory = factoriesPair.getImportModuleFactory();
    DatabaseModuleFactory exportModuleFactory = factoriesPair.getExportModuleFactory();

    // get appropriate command line options
    CommandLineParser commandLineParser = new DefaultParser();
    CommandLine commandLine;
    Options options = new Options();

    HashMap<String, Parameter> mapOptionToParameter = new HashMap<String, Parameter>();

    for (Parameter parameter : importModuleFactory.getImportModuleParameters().getParameters()) {
      Option option = parameter.toOption("i", "import");
      options.addOption(option);
      mapOptionToParameter.put(getUniqueOptionIdentifier(option), parameter);
    }

    for (ParameterGroup parameterGroup : importModuleFactory.getImportModuleParameters().getGroups()) {
      OptionGroup optionGroup = parameterGroup.toOptionGroup("i", "import");
      options.addOptionGroup(optionGroup);

      for (Parameter parameter : parameterGroup.getParameters()) {
        mapOptionToParameter.put(getUniqueOptionIdentifier(parameter.toOption("i", "import")), parameter);
      }
    }

    for (Parameter parameter : exportModuleFactory.getExportModuleParameters().getParameters()) {
      Option option = parameter.toOption("e", "export");
      options.addOption(option);
      mapOptionToParameter.put(getUniqueOptionIdentifier(option), parameter);
    }

    for (ParameterGroup parameterGroup : exportModuleFactory.getExportModuleParameters().getGroups()) {
      OptionGroup optionGroup = parameterGroup.toOptionGroup("e", "export");
      options.addOptionGroup(optionGroup);

      for (Parameter parameter : parameterGroup.getParameters()) {
        mapOptionToParameter.put(getUniqueOptionIdentifier(parameter.toOption("e", "export")), parameter);
      }
    }

    Option importOption = Option.builder("i").longOpt("import").hasArg().optionalArg(false).build();
    Option exportOption = Option.builder("e").longOpt("export").hasArg().optionalArg(false).build();
    options.addOption(importOption);
    options.addOption(exportOption);

    // new HelpFormatter().printHelp(80, "dbptk", "\nModule Options:", options,
    // null, true);

    // parse the command line arguments with those options
    try {
      commandLine = commandLineParser.parse(options, args.toArray(new String[] {}), false);
      if (!commandLine.getArgList().isEmpty()) {
        throw new ParseException("Unrecognized option: " + commandLine.getArgList().get(0));
      }
    } catch (MissingOptionException e) {
      // use long names instead of short names in the error message
      List<String> missingShort = e.getMissingOptions();
      List<String> missingLong = new ArrayList<String>();
      for (String shortOption : missingShort) {
        missingLong.add(options.getOption(shortOption).getLongOpt());
      }
      LOGGER.debug("MissingOptionException (the original, unmodified exception)", e);
      throw new MissingOptionException(missingLong);
    }

    // create arguments to pass to factory
    HashMap<Parameter, String> importModuleArguments = new HashMap<Parameter, String>();
    HashMap<Parameter, String> exportModuleArguments = new HashMap<Parameter, String>();
    for (Option option : commandLine.getOptions()) {
      Parameter p = mapOptionToParameter.get(getUniqueOptionIdentifier(option));
      if (p != null) {
        if (isImportModuleOption(option)) {
          if (p.hasArgument()) {
            importModuleArguments.put(p, option.getValue(p.valueIfNotSet()));
          } else {
            importModuleArguments.put(p, p.valueIfSet());
          }
        } else if (isExportModuleOption(option)) {
          if (p.hasArgument()) {
            exportModuleArguments.put(p, option.getValue(p.valueIfNotSet()));
          } else {
            exportModuleArguments.put(p, p.valueIfSet());
          }
        } else {
          throw new ParseException("Unexpected parse exception occurred.");
        }
      }
    }
    return new DatabaseModuleFactoriesArguments(importModuleArguments, exportModuleArguments);
  }

  private void printHelp(PrintStream printStream) {
    StringBuilder out = new StringBuilder();

    Set<String> visibleModules;

    if (commandLineArguments.size() <= 1) {
      // print module list
      visibleModules = new HashSet<>();
    } else {
      visibleModules = new HashSet<>(commandLineArguments);
    }

    out.append("Database Preservation Toolkit").append(MiscUtils.APP_NAME_AND_VERSION)
      .append("\nMore info: http://www.database-preservation.com").append("\n")
      .append("\nUsage: dbptk <importModule> [import module options] <exportModule> [export module options]\n");

    ArrayList<DatabaseModuleFactory> modulesList = new ArrayList<>(factories);
    Collections.sort(modulesList, new DatabaseModuleFactoryNameComparator());

    if (visibleModules.isEmpty()) {
      String spaceSmall = "      ";
      String spaceMedium = spaceSmall + "  ";

      out.append("\n").append(spaceSmall).append("For help on specific modules use:\n").append(spaceSmall)
        .append("dbptk -h|help [modules...]\n");

      out.append("\n").append(spaceSmall).append("Import modules: \n");

      for (DatabaseModuleFactory factory : modulesList) {
        if (factory.producesImportModules()) {
          out.append(spaceMedium).append(factory.getModuleName()).append("\n");
        }
      }

      out.append("\n\n").append(spaceSmall).append("Export modules: \n");
      for (DatabaseModuleFactory factory : modulesList) {
        if (factory.producesExportModules()) {
          out.append(spaceMedium).append(factory.getModuleName()).append("\n");
        }
      }

    } else {
      try {
        for (DatabaseModuleFactory factory : modulesList) {
          if (factory.producesImportModules() && visibleModules.contains(factory.getModuleName())) {
            out.append(
              printModuleHelp("Import module: -i " + factory.getModuleName() + ", --import=" + factory.getModuleName(),
                "i", "import", factory.getImportModuleParameters()));
          }

          if (factory.producesExportModules() && visibleModules.contains(factory.getModuleName())) {
            out.append(
              printModuleHelp("Export module: -e " + factory.getModuleName() + ", --export=" + factory.getModuleName(),
                "e", "export", factory.getExportModuleParameters()));
          }
        }
      } catch (UnsupportedModuleException e) {
        throw new UnreachableException(e);
      }
    }

    out.append("\n");
    printStream.append(out).flush();
  }

  private String printModuleHelp(String moduleDesignation, String shortParameterPrefix, String longParameterPrefix,
    Parameters moduleParameters) {
    StringBuilder out = new StringBuilder();

    String space = "      ";

    out.append("\n").append(space).append(moduleDesignation);

    for (Parameter parameter : moduleParameters.getParameters()) {
      out.append(printParameterHelp(space, shortParameterPrefix, longParameterPrefix, parameter));
    }

    for (ParameterGroup parameterGroup : moduleParameters.getGroups()) {
      for (Parameter parameter : parameterGroup.getParameters()) {
        out.append(printParameterHelp(space, shortParameterPrefix, longParameterPrefix, parameter));
      }
    }
    out.append("\n");

    return out.toString();
  }

  private String printParameterHelp(String space, String shortPrefix, String longPrefix, Parameter parameter) {
    StringBuilder out = new StringBuilder();

    out.append("\n").append(space).append(space);

    if (StringUtils.isNotBlank(parameter.shortName())) {
      out.append("-").append(shortPrefix).append(parameter.shortName()).append(", ");
    }

    out.append("--").append(longPrefix).append("-").append(parameter.longName());

    if (parameter.hasArgument()) {
      if (parameter.isOptionalArgument()) {
        out.append("[");
      }
      out.append("=value");
      if (parameter.isOptionalArgument()) {
        out.append("]");
      }
    }

    out.append("\n").append(space).append(space).append(space);
    String description = (parameter.required() ? "(required) " : "(optional) ") + parameter.description();
    out.append(WordUtils.wrap(description, Constants.CLI_LINE_WIDTH, "\n" + StringUtils.repeat(space, 3), false));

    return out.toString();
  }

  /**
   * Get operating system information.
   *
   * @return An order-preserving map which keys are a description (String) of the
   *         information contained in the values (which are also of type String).
   */
  private HashMap<String, String> getOperatingSystemInfo() {
    LinkedHashMap<String, String> result = new LinkedHashMap<>();

    result.put("Operating system", System.getProperty("os.name", "unknown"));
    result.put("Architecture", System.getProperty("os.arch", "unknown"));
    result.put("Version", System.getProperty("os.version", "unknown"));
    result.put("Java vendor", System.getProperty("java.vendor", "unknown"));
    result.put("Java version", System.getProperty("java.version", "unknown"));
    result.put("Java class version", System.getProperty("java.class.version", "unknown"));
    // Charset.defaultCharset() is bugged on java version 5 and fixed on java 6
    result.put("Default Charset reported by java", Charset.defaultCharset().toString());
    result.put("Default Charset used by StreamWriter", getDefaultCharSet());
    result.put("file.encoding property", System.getProperty("file.encoding"));

    return result;
  }

  /**
   * Logs operating system information
   */
  public void logOperatingSystemInfo() {
    for (Map.Entry<String, String> entry : getOperatingSystemInfo().entrySet()) {
      LOGGER.debug(entry.getKey() + ": " + entry.getValue());
    }
  }

  public boolean shouldPrintHelp() {
    if (commandLineArguments.isEmpty()) {
      return true;
    } else {
      String arg = commandLineArguments.get(0);
      return "-h".equalsIgnoreCase(arg) || "--help".equalsIgnoreCase(arg) || "help".equalsIgnoreCase(arg);
    }
  }

  public boolean usingUTF8() {
    return Charset.defaultCharset().equals(Charset.forName("UTF-8"));
  }

  private static class DatabaseModuleFactoryNameComparator implements Comparator<DatabaseModuleFactory> {
    @Override
    public int compare(DatabaseModuleFactory o1, DatabaseModuleFactory o2) {
      return o1.getModuleName().compareTo(o2.getModuleName());
    }
  }

  private static String getUniqueOptionIdentifier(Option option) {
    // some string that should never occur in option shortName nor longName
    final String delimiter = "\r\f\n";
    return new StringBuilder().append(delimiter).append(option.getOpt()).append(delimiter).append(option.getLongOpt())
      .append(delimiter).toString();
  }

  private static boolean isImportModuleOption(Option option) {
    final String type = "i";
    if (StringUtils.isNotBlank(option.getOpt())) {
      return option.getOpt().startsWith(type);
    } else if (StringUtils.isNotBlank(option.getLongOpt())) {
      return option.getLongOpt().startsWith(type);
    }
    return false;
  }

  private static boolean isExportModuleOption(Option option) {
    final String type = "e";
    if (StringUtils.isNotBlank(option.getOpt())) {
      return option.getOpt().startsWith(type);
    } else if (StringUtils.isNotBlank(option.getLongOpt())) {
      return option.getLongOpt().startsWith(type);
    }
    return false;
  }

  private static String getDefaultCharSet() {
    OutputStreamWriter dummyWriter = new OutputStreamWriter(new ByteArrayOutputStream());
    String encoding = dummyWriter.getEncoding();
    return encoding;
  }

  /**
   * Pair containing the import and export module factories
   */
  public class DatabaseModuleFactoriesPair {
    // left: import, right: export
    private final ImmutablePair<DatabaseModuleFactory, DatabaseModuleFactory> factories;

    /**
     * Create a new pair with an import module factory and an export module factory
     *
     * @param importModuleFactory
     *          the import module factory
     * @param exportModuleFactory
     *          the export module factory
     */
    public DatabaseModuleFactoriesPair(DatabaseModuleFactory importModuleFactory,
      DatabaseModuleFactory exportModuleFactory) {
      factories = new ImmutablePair<DatabaseModuleFactory, DatabaseModuleFactory>(importModuleFactory,
        exportModuleFactory);
    }

    /**
     * @return the import module
     */
    public DatabaseModuleFactory getImportModuleFactory() {
      return factories.getLeft();
    }

    /**
     * @return the import module
     */
    public DatabaseModuleFactory getExportModuleFactory() {
      return factories.getRight();
    }
  }

  /**
   * Pair containing the arguments to create the import and export modules
   */
  public class DatabaseModuleFactoriesArguments {
    // left: import, right: export
    private final ImmutablePair<Map<Parameter, String>, Map<Parameter, String>> factories;

    /**
     * Create a new pair with the import module arguments and the export module
     * arguments
     *
     * @param importModuleArguments
     *          import module arguments in the form Map<parameter, value parsed from
     *          the command line>
     * @param exportModuleArguments
     *          export module arguments in the form Map<parameter, value parsed from
     *          the command line>
     */
    public DatabaseModuleFactoriesArguments(Map<Parameter, String> importModuleArguments,
      Map<Parameter, String> exportModuleArguments) {
      factories = new ImmutablePair<Map<Parameter, String>, Map<Parameter, String>>(importModuleArguments,
        exportModuleArguments);
    }

    /**
     * @return import module arguments in the form Map<parameter, value parsed from
     *         the command line>
     */
    public Map<Parameter, String> getImportModuleArguments() {
      return factories.getLeft();
    }

    /**
     * @return export module arguments in the form Map<parameter, value parsed from
     *         the command line>
     */
    public Map<Parameter, String> getExportModuleArguments() {
      return factories.getRight();
    }
  }
}
