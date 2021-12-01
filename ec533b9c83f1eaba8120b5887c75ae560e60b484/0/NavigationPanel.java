package com.databasepreservation.common.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NavigationPanel extends Composite {
  interface NavigationPanelUiBinder extends UiBinder<Widget, NavigationPanel> {
  }

  private static NavigationPanelUiBinder binder = GWT.create(NavigationPanelUiBinder.class);

  @UiField
  FlowPanel navigationPanelHeader;

  @UiField
  FlowPanel navigationPanelDescription;

  @UiField
  FlowPanel navigationPanelInfo;

  @UiField
  FlowPanel navigationPanelOptions;

  public static NavigationPanel createInstance(String title) {
    return new NavigationPanel(title);
  }

  private NavigationPanel(String title) {
    initWidget(binder.createAndBindUi(this));

    Label l = new Label();
    l.setText(title);
    navigationPanelHeader.add(l);
  }

  public void addToDescriptionPanel(String description) {
    Label l = new Label();
    l.setText(description);

    navigationPanelDescription.add(l);
  }

  public void addToDescriptionPanel(SafeHtml description) {
    final InlineHTML inlineHTML = new InlineHTML();
    inlineHTML.setHTML(description);

    navigationPanelDescription.add(inlineHTML);
  }

  public void addToInfoPanel(Widget widget) {
    this.navigationPanelInfo.add(widget);
  }

  public void addButton(Button button) {
    this.navigationPanelOptions.add(button);
  }

  public void addButton(FlowPanel button) {
    this.navigationPanelOptions.add(button);
  }

  public void clearButtonsPanel() { this.navigationPanelOptions.clear(); }
}