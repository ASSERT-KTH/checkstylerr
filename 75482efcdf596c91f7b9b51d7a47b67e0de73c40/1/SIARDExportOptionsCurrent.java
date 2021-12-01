package com.databasepreservation.main.desktop.client.dbptk.wizard.common.exportOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.desktop.FileUploadField;
import com.databasepreservation.main.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.common.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.client.common.dialogs.Dialogs;
import com.databasepreservation.main.desktop.shared.models.Filter;
import com.databasepreservation.main.common.shared.models.PreservationParameter;
import com.databasepreservation.main.common.shared.models.wizardParameters.ExportOptionsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDExportOptionsCurrent extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDExportOptionsCurrentUiBinder extends UiBinder<Widget, SIARDExportOptionsCurrent> {
  }

  private static SIARDExportOptionsCurrentUiBinder binder = GWT.create(SIARDExportOptionsCurrentUiBinder.class);

  @UiField
  FlowPanel content;

  private static HashMap<String, SIARDExportOptionsCurrent> instances = new HashMap<>();
  private HashMap<String, TextBox> textBoxInputs = new HashMap<>();
  private HashMap<String, CheckBox> checkBoxInputs = new HashMap<>();
  private HashMap<String, String> fileInputs = new HashMap<>();
  private DBPTKModule dbptkModule;
  private ArrayList<PreservationParameter> parameters;
  private ArrayList<Label> externalLobsLabels = new ArrayList<>();
  private HashMap<String, TextBox> externalLobsInputs = new HashMap<>();
  private CheckBox externalLobCheckbox;
  private int validationError = -1;
  private String version;

  public static SIARDExportOptionsCurrent getInstance(String key, DBPTKModule dbptkModule) {
    if (instances.get(key) == null) {
      instances.put(key, new SIARDExportOptionsCurrent(key, dbptkModule));
    }
    return instances.get(key);
  }

  private SIARDExportOptionsCurrent(String version, DBPTKModule dbptkModule) {
    initWidget(binder.createAndBindUi(this));

    this.version = version;
    this.parameters = dbptkModule.getParameters(version);
    this.dbptkModule = dbptkModule;

    FlowPanel panel = new FlowPanel();

    for (PreservationParameter p : parameters) {
      if (p.getExportOption() != null) {
        if (p.getExportOption().equals(ViewerConstants.SIARD_EXPORT_OPTIONS)) {
          buildGenericWidget(p);
        } else if (p.getExportOption().equals(ViewerConstants.EXTERNAL_LOBS_EXPORT_OPTIONS)) {
          buildExternalLobs(p, panel);
        }
      }
    }
  }

  public ExportOptionsParameters getValues() {
    ExportOptionsParameters exportOptionsParameters = new ExportOptionsParameters();

    HashMap<String, String> exportParameters = new HashMap<>();

    for (PreservationParameter parameter : parameters) {
      switch (parameter.getInputType()) {
        case ViewerConstants.INPUT_TYPE_CHECKBOX:
          if (checkBoxInputs.get(parameter.getName()) != null) {
            final boolean value = checkBoxInputs.get(parameter.getName()).getValue();
            exportParameters.put(parameter.getName(), String.valueOf(value));
          }
          break;
        case ViewerConstants.INPUT_TYPE_TEXT:
          if (textBoxInputs.get(parameter.getName()) != null) {
            final String text = textBoxInputs.get(parameter.getName()).getText();
            exportParameters.put(parameter.getName(), text);
          }
          if (externalLobCheckbox != null && externalLobCheckbox.getValue()) {
            if (externalLobsInputs.get(parameter.getName()) != null) {
              final String text = externalLobsInputs.get(parameter.getName()).getText();
              exportParameters.put(parameter.getName(), text);
            }
          }
          break;
        case ViewerConstants.INPUT_TYPE_FOLDER:
        case ViewerConstants.INPUT_TYPE_FILE:
          if (fileInputs.get(parameter.getName()) != null) {
            final String path = fileInputs.get(parameter.getName());
            exportOptionsParameters.setSiardPath(path);
            exportParameters.put(parameter.getName(), path);
          }
          break;
        case ViewerConstants.INPUT_TYPE_DEFAULT:
          if (parameter.getName().equals("pretty-xml")) {
            exportParameters.put(parameter.getName(), "true");
          }
        case ViewerConstants.INPUT_TYPE_NONE:
        default:
          break;
      }
    }

    exportOptionsParameters.setSIARDVersion(version);
    exportOptionsParameters.setParameters(exportParameters);

    return exportOptionsParameters;
  }

  public int validate() {
    if (validateExternalLobs() != SIARDExportOptions.OK) {
      validationError = SIARDExportOptions.EXTERNAL_LOBS_ERROR;
      return SIARDExportOptions.EXTERNAL_LOBS_ERROR;
    }

    final ArrayList<PreservationParameter> requiredParameters = dbptkModule.getRequiredParameters(version);

    for (PreservationParameter parameter : requiredParameters) {
      switch (parameter.getInputType()) {
        case "TEXT":
          if (textBoxInputs.get(parameter.getName()) != null) {
            final TextBox textBox = textBoxInputs.get(parameter.getName());
            if (ViewerStringUtils.isBlank(textBox.getText())) {
              validationError = SIARDExportOptions.MISSING_FIELD;
              return SIARDExportOptions.MISSING_FIELD;
            } else {
              validationError = SIARDExportOptions.MISSING_FIELD;
              return SIARDExportOptions.MISSING_FIELD;
            }
          }
          break;
        case "FOLDER":
        case "FILE":
          if (fileInputs.get(parameter.getName()) != null) {
            final String s = fileInputs.get(parameter.getName());
            if (ViewerStringUtils.isBlank(s)) {
              validationError = SIARDExportOptions.MISSING_FILE;
              return SIARDExportOptions.MISSING_FILE;
            }
          } else {
            validationError = SIARDExportOptions.MISSING_FILE;
            return SIARDExportOptions.MISSING_FILE;
          }
          break;
        default:
          ;
      }
    }
    return SIARDExportOptions.OK;
  }

  public void clear() {
    instances.clear();
  }

  public void error() {
    if (validationError != -1) {
      Toast.showError(messages.errorMessagesExportOptionsTitle(), messages.errorMessagesExportOptions(validationError));
    }
  }

  private int validateExternalLobs() {
    if (externalLobCheckbox != null && externalLobCheckbox.getValue()) {
      for (TextBox textBox : externalLobsInputs.values()) {
        final String text = textBox.getText();
        if (ViewerStringUtils.isBlank(text)) {
          return SIARDExportOptions.MISSING_FIELD;
        }
      }
    }
    return SIARDExportOptions.OK;
  }

  private void updateCheckboxExternalLobs(boolean value) {
    if (value) { // selected
      for (Label label : externalLobsLabels) {
        label.addStyleName("gwt-Label");
        label.removeStyleName("gwt-Label-disabled");
      }

      for (TextBox textBox : externalLobsInputs.values()) {
        textBox.setEnabled(true);
      }
    } else {
      for (Label label : externalLobsLabels) {
        label.removeStyleName("gwt-Label");
        label.addStyleName("gwt-Label-disabled");
      }

      for (TextBox textBox : externalLobsInputs.values()) {
        textBox.setEnabled(false);
      }
    }
  }

  private void buildExternalLobs(PreservationParameter parameter, FlowPanel panel) {
    GenericField genericField;

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_CHECKBOX:
        externalLobCheckbox = new CheckBox();
        externalLobCheckbox.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        externalLobCheckbox.addStyleName("form-checkbox");
        externalLobCheckbox.addValueChangeHandler(event -> {
          updateCheckboxExternalLobs(event.getValue());
        });
        genericField = GenericField.createInstance(externalLobCheckbox);
        genericField.setRequired(parameter.isRequired());
        genericField.setCSSMetadata("form-row", "form-label-spaced");
        content.add(genericField);
        break;
      case ViewerConstants.INPUT_TYPE_TEXT:
        Label label = new Label();
        label.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        externalLobsLabels.add(label);
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox-external-lobs");
        externalLobsInputs.put(parameter.getName(), defaultTextBox);
        Label label_end = new Label();
        label_end.setText(messages.wizardExportOptionsLabels(parameter.getName() + "-end"));
        externalLobsLabels.add(label_end);
        if (version.equals(ViewerConstants.SIARDDK)) {
          label.addStyleName("form-label");
          label_end.addStyleName("form-label");
        } else {
          label.addStyleName("form-label gwt-Label-disabled");
          label_end.addStyleName("form-label gwt-Label-disabled");
          defaultTextBox.setEnabled(false);
        }
        FlowPanel formHelper = new FlowPanel();
        formHelper.addStyleName("form-helper");
        FlowPanel formRow = new FlowPanel();
        formRow.addStyleName("form-row");
        formRow.add(label);
        formRow.add(defaultTextBox);
        formRow.add(label_end);
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));
        formHelper.add(formRow);
        formHelper.add(span);
        panel.add(formHelper);
        panel.addStyleName("form-lobs");
        content.add(panel);
        break;
      default:
        break;
    }
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_CHECKBOX:
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        break;
      case ViewerConstants.INPUT_TYPE_FILE:
        FileUploadField fileUploadField = FileUploadField
          .createInstance(messages.wizardExportOptionsLabels(parameter.getName()), messages.basicActionBrowse());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
            JavaScriptObject.createArray();
            Filter filter = new Filter();
            filter.setName(ViewerConstants.SIARD_FILES);
            filter.setExtensions(Collections.singletonList(ViewerConstants.SIARD_SUFFIX));
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.emptyList(),
              Collections.singletonList(filter));
            String path = JavascriptUtils.saveFileDialog(options);
            if (path != null) {
              fileInputs.put(parameter.getName(), path);
              fileUploadField.setPathLocation(path, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          } else {
            // TODO
            // fileInputs.put(parameter.getName(), path);
          }
        });
        FlowPanel helper = new FlowPanel();
        helper.addStyleName("form-helper");
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));

        helper.add(fileUploadField);
        helper.add(span);
        content.add(helper);
        break;
      case ViewerConstants.INPUT_TYPE_FOLDER:
        FileUploadField folder = FileUploadField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()),
          messages.basicActionBrowse());
        folder.setParentCSS("form-row");
        folder.setLabelCSS("form-label-spaced");
        folder.setButtonCSS("btn btn-link form-button");
        folder.setRequired(parameter.isRequired());
        folder.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openDirectory"),
              Collections.emptyList());
            String path = JavascriptUtils.openFileDialog(options);
            if (path != null) {
              fileInputs.put(parameter.getName(), path);
              folder.setPathLocation(path, path);
              folder.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        content.add(folder);
        break;
      case ViewerConstants.INPUT_TYPE_COMBOBOX:
      case ViewerConstants.INPUT_TYPE_NONE:
        genericField = null;
        break;
      case ViewerConstants.INPUT_TYPE_NUMBER:
      case ViewerConstants.INPUT_TYPE_TEXT:
      default:
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        genericField = GenericField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()),
          defaultTextBox);
        break;
    }

    if (genericField != null) {
      FlowPanel helper = new FlowPanel();
      helper.addStyleName("form-helper");
      InlineHTML span = new InlineHTML();
      span.addStyleName("form-text-helper text-muted");
      span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));

      genericField.setRequired(parameter.isRequired());
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      genericField.addHelperText(span);
      helper.add(genericField);
      helper.add(span);
      content.add(helper);
    }
  }
}