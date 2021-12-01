package com.databasepreservation.common.client.common.visualization.manager.databasePanel.admin;

import java.util.List;

import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.helpers.HelperUploadSIARDFile;
import com.databasepreservation.common.client.common.lists.DatabaseList;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DatabaseManage extends ContentPanel {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forManageDatabase();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  interface ManageUiBinder extends UiBinder<Widget, DatabaseManage> {
  }

  private static ManageUiBinder binder = GWT.create(ManageUiBinder.class);

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  SimplePanel mainHeader;

  @UiField
  SimplePanel description;

  @UiField(provided = true)
  DatabaseList databaseList;

  @UiField
  Button create;

  @UiField
  Button open;

  private static DatabaseManage instance = null;

  public static DatabaseManage getInstance() {
    if (instance == null) {
      instance = new DatabaseManage();
    }
    return instance;
  }

  private DatabaseManage() {
    databaseList = new DatabaseList();
    initWidget(binder.createAndBindUi(this));

    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SERVER),
        messages.menusidebar_databases(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.manageDatabasePageDescription());
    instance.setCSS("table-row-description", "font-size-description");

    description.setWidget(instance);

    searchInputBox.getElement().setPropertyString("placeholder", messages.searchPlaceholder());

    searchInputBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(event -> doSearch());

    databaseList.getSelectionModel().addSelectionChangeHandler(event -> {
      ViewerDatabase selected = databaseList.getSelectionModel().getSelectedObject();
      if (selected != null) {
        if(ApplicationType.getType().equals(ViewerConstants.SERVER)){
          HistoryManager.gotoSIARDInfo(selected.getUuid());
        }
        databaseList.getSelectionModel().clear();
      }
    });

    initButtons();
  }

  private void initButtons() {
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      create.addClickHandler(event -> HistoryManager.gotoCreateSIARD());
      open.addClickHandler(event -> new HelperUploadSIARDFile().openFile(databaseList));
    } else {
      create.setText(messages.managePageButtonTextForDownloadDBPTK());
      create.addClickHandler(event -> {
        Window.open("https://database-preservation.com/#desktop", "_blank", "");
      });
      open.addClickHandler(event -> HistoryManager.gotoNewUpload());
    }
  }

  private void doSearch() {
    // start searching
    Filter filter;
    String searchText = searchInputBox.getText();
    if (ViewerStringUtils.isBlank(searchText)) {
      filter = ViewerConstants.DEFAULT_FILTER;
    } else {
      filter = new Filter(new BasicSearchFilterParameter(ViewerConstants.INDEX_SEARCH, searchText));
    }

    databaseList.setFilter(filter);
  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  @Override
  protected void onLoad() {
    super.onLoad();
    databaseList.getSelectionModel().clear();
  }
}