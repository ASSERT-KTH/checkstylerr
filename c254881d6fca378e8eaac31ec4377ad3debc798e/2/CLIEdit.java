/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/db-preservation-toolkit
 */
package com.databasepreservation.cli;

import com.databasepreservation.Constants;
import com.databasepreservation.model.exception.TooMuchArgumentsException;
import com.databasepreservation.model.modules.edits.EditModuleFactory;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.ParameterGroup;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CLIEdit extends CLIHandler {

  public static final String LIST_OPTION = "list";
  public static final String SET_OPTION = "set";
  public static final String NO_OPTION = "none";

  private final ArrayList<EditModuleFactory> allEditFactories;
  private EditModuleFactory editModuleFactory;
  private Map<Parameter, List<String>> editModuleParameters;
  private String siardPackage;

  public CLIEdit(List<String> commandLineArguments, Collection<EditModuleFactory> editModuleFactories) {
    super(commandLineArguments);
    allEditFactories = new ArrayList<>(editModuleFactories);
  }

  /**
   * Gets the edit module parameters, obtained by parsing the parameters
   *
   * @return The edit module configuration parameters
   * @throws ParseException
   *           if there was an error parsing the command line parameters
   */
  public Map<Parameter, List<String>> getEditModuleParameters() throws ParseException {
    if (editModuleFactory == null) {
      parse(commandLineArguments);
    }
    return editModuleParameters;
  }

  /**
   * Gets the edit module factory, obtained by parsing the parameters
   *
   * @return The edit module configuration parameters
   * @throws ParseException
   *           if there was an error parsing the command line parameters
   */
  public EditModuleFactory getEditModuleFactory() throws ParseException {
    if (editModuleFactory == null) {
      parse(commandLineArguments);
    }
    return editModuleFactory;
  }

  /**
   * Gets the path to the SIARD archive
   *
   * @return The path to the SIARD archive
   */
  public String getSIARDPackage() {
    return siardPackage;
  }

  /**
   * Gets the option name. Note that this method does not trigger
   * the lazy loading mechanism for parsing the parameters, so the value may be
   * NO_OPTION if no calls to {@code getEditModuleParameters()} were made.
   *
   * @return The option name. NO_OPTION if the command line parameters have not
   *         been parsed yet
   */
  public String option() {

    if (editModuleParameters.isEmpty()) return NO_OPTION;

    if (editModuleParameters.size() == 1) return NO_OPTION;

    for (Parameter p : editModuleParameters.keySet()) {
      if (p.longName().equalsIgnoreCase("list") || p.shortName().equalsIgnoreCase("l")) {
        return LIST_OPTION;
      }
      if (p.longName().equalsIgnoreCase("set") || p.shortName().equalsIgnoreCase("s")) {
        return SET_OPTION;
      }
    }

    return NO_OPTION;
  }

  // Auxiliary Internal Methods

  private void parse(List<String> args) throws ParseException {
    EditModuleFactory factory = getEditFactory(args);

    editModuleParameters = getEditArguments(factory, args);
  }

  private HashMap<Parameter, List<String>> getEditArguments(EditModuleFactory factory, List<String> args)
    throws ParseException {
    // get appropriate command line options
    CommandLineParser commandLineParser = new DefaultParser();
    CommandLine commandLine;
    Options options = new Options();

    HashMap<String, Parameter> mapOptionToParameter = new HashMap<>();

    for (Parameter parameter : factory.getImportParameters().getParameters()) {
      Option option = parameter.toOption("i", "import");
      options.addOption(option);
      mapOptionToParameter.put(getUniqueOptionIdentifier(option), parameter);
    }

    for (ParameterGroup parameterGroup : factory.getImportParameters().getGroups()) {
      OptionGroup optionGroup = parameterGroup.toOptionGroup("i", "import");
      options.addOptionGroup(optionGroup);

      for (Parameter parameter : parameterGroup.getParameters()) {
        mapOptionToParameter.put(getUniqueOptionIdentifier(parameter.toOption("i", "import")), parameter);
      }
    }

    for (Parameter parameter : factory.getParameters().getParameters()) {
      Option option = parameter.toOption();
      options.addOption(option);
      mapOptionToParameter.put(getUniqueOptionIdentifier(option), parameter);
    }

    commandLine = commandLineParse(commandLineParser, options, args);

    // create arguments to pass to factory
    HashMap<Parameter, List<String>> editModuleArguments = new HashMap<>();

    for (Option option : commandLine.getOptions()) {
      Parameter p = mapOptionToParameter.get(getUniqueOptionIdentifier(option));
      if (p != null) {
        if (isImportOption(option)) {
          if (p.hasArgument()) {
            if (p.longName().contentEquals("file")) {
              siardPackage = option.getValue(p.valueIfNotSet());
            }
            ArrayList<String> values = updateArgs(editModuleArguments, p, option.getValue(p.valueIfNotSet()));
            editModuleArguments.put(p, values);
          } else {
            if (p.longName().contentEquals("file")) {
              siardPackage = option.getValue(p.valueIfSet());
            }
            ArrayList<String> values = updateArgs(editModuleArguments, p, option.getValue(p.valueIfSet()));
            editModuleArguments.put(p, values);
          }
        } else if (isSetFieldOption(option)) {
          if (p.hasArgument()) {

            if (option.getValues().length >= 6) {
              throw new TooMuchArgumentsException("Too much arguments for the " + option.getLongOpt() + " option");
            }

            StringBuilder sb = new StringBuilder();
            for (String s : option.getValues()) {
              sb.append(s).append(Constants.SEPARATOR);
            }
            ArrayList<String> values = updateArgs(editModuleArguments, p, sb.toString());
            editModuleArguments.put(p, values);
          }
        } else if (isListOption(option)) {
          if (p.hasArgument()) {

          } else {
            ArrayList<String> values = updateArgs(editModuleArguments, p, option.getValue(p.valueIfNotSet()));
            editModuleArguments.put(p, values);
          }

        } else {
          throw new ParseException("Unexpected parse exception occurred.");
        }
      }
    }
    return editModuleArguments;
  }

  private ArrayList<String> updateArgs(HashMap<Parameter, List<String>> arguments, Parameter parameter, String value) {
    ArrayList<String> values = new ArrayList<>();

    if (arguments.get(parameter) != null) {
      values = new ArrayList<>(arguments.get(parameter));
    }

    values.add(value);

    return values;
  }

  private EditModuleFactory getEditFactory(List<String> args) throws ParseException {
    for (EditModuleFactory factory : allEditFactories) {
      String moduleName = factory.getModuleName();
      if (moduleName.equalsIgnoreCase("edit-siard") && factory.isEnabled()) {
        editModuleFactory = factory;
      }
    }

    if (editModuleFactory == null) {
      throw new ParseException("Invalid edit module.");
    }

    return editModuleFactory;
  }

  private static boolean isImportOption(Option option) {
    final String type = "i";
    if (StringUtils.isNotBlank(option.getOpt())) {
      return option.getOpt().startsWith(type);
    } else if (StringUtils.isNotBlank(option.getLongOpt())) {
      return option.getLongOpt().startsWith(type);
    }
    return false;
  }

  private static boolean isSetFieldOption(Option option) {
    final String typeShot = "s";
    final String typeLong = "set";
    if (StringUtils.isNotBlank(option.getOpt())) {
      return option.getOpt().contentEquals(typeShot);
    } else if (StringUtils.isNotBlank(option.getLongOpt())) {
      return option.getLongOpt().contentEquals(typeLong);
    }
    return false;
  }

  private static boolean isListOption(Option option) {
    final String typeShot = "l";
    final String typeLong = "list";
    if (StringUtils.isNotBlank(option.getOpt())) {
      return option.getOpt().contentEquals(typeShot);
    } else if (StringUtils.isNotBlank(option.getLongOpt())) {
      return option.getLongOpt().contentEquals(typeLong);
    }
    return false;
  }
}
