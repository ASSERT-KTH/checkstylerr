/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.main.visualization.client.common.search;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Dropdown extends Composite implements HasValueChangeHandlers<String> {
  private FocusPanel focusPanel;
  private SimplePanel panel;
  private Label selectedLabel;
  private PopupPanel popup;
  private VerticalPanel popupPanel;
  private boolean popupShowing = false;

  private Map<String, String> popupValues;

  public Dropdown() {
    focusPanel = new FocusPanel();
    panel = new SimplePanel();
    selectedLabel = new Label();
    popup = new PopupPanel(true);
    popupPanel = new VerticalPanel();
    popupValues = new HashMap<String, String>();

    panel.add(selectedLabel);
    focusPanel.add(panel);

    focusPanel.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        popup();
      }
    });

    popup.addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        panel.removeStyleName("open");
      }
    });

    initWidget(focusPanel);

    focusPanel.addStyleName("dropdown");
    panel.addStyleName("dropdown-panel");
    selectedLabel.addStyleName("dropdown-label");
    popup.addStyleName("dropdown-popup");
  }

  public void addItem(final String label, final String value) {
    popupValues.put(label, value);
    Label item = new Label(label);
    popupPanel.add(item);
    item.addStyleName("dropdown-item");

    item.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        selectedLabel.setText(label);
        onChange();
        popup();
      }
    });

    popup.setWidget(popupPanel);
    setPanelWidth();
  }

  public void setLabel(String text) {
    selectedLabel.setText(text);
  }

  public void popup() {
    if (popupShowing) {
      popup.hide();
      popupShowing = popup.isShowing();
    } else {
      popup.showRelativeTo(panel);
      popupShowing = popup.isShowing();
      panel.addStyleName("open");
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getSelectedValue());
  }

  public String getSelectedValue() {
    return popupValues.get(selectedLabel.getText());
  }

  public void setPanelWidth() {
    boolean visible = popup.isVisible();
    popup.setVisible(true);
    focusPanel.getElement().setAttribute("min-width", popup.getOffsetWidth() + "px");
    popup.setVisible(visible);
  }

  public void addPopupStyleName(String styleName) {
    popup.addStyleName(styleName);
  }
}
