package com.databasepreservation.main.desktop.client.dbptk;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.desktop.client.common.Card;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HomePage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface HomePageUiBinder extends UiBinder<Widget, HomePage> {
  }

  private static HomePageUiBinder binder = GWT.create(HomePageUiBinder.class);

  @UiField
  FlowPanel options;

  private static HomePage instance = null;

  public static HomePage getInstance() {
    if (instance == null) {
      instance = new HomePage();
    }
    return instance;
  }

  private HomePage() {
    initWidget(binder.createAndBindUi(this));

    init();
  }

  private void init() {
    Button btnCreate = new Button();
    btnCreate.setText(messages.createCardButton());

    btnCreate.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // TODO: OPEN MIGRATE PAGE
      }
    });

    Button btnOpen = new Button();
    btnOpen.setText(messages.openCardButton());

    btnOpen.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        String path;
        if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
          path = JavascriptUtils.openFileDialog();
        } else {
          path = "/home/mguimaraes/Desktop/mysql.siard";
        }

        BrowserService.Util.getInstance().findSIARDFile(path, new DefaultAsyncCallback<String>() {
          @Override
          public void onSuccess(String databaseUUID) {
            if (databaseUUID != null) {
              Dialogs.showConfirmDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
                messages.dialogCancel(), messages.dialogConfirm(), new DefaultAsyncCallback<Boolean>() {
                  @Override
                  public void onSuccess(Boolean confirm) {
                    if (confirm) {
                      BrowserService.Util.getInstance().uploadMetadataSIARD(path, new DefaultAsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable caught) {
                          // TODO: error handling
                        }

                        @Override
                        public void onSuccess(String newDatabaseUUID) {
                          HistoryManager.gotoSIARDInfo(newDatabaseUUID);
                        }
                      });
                    } else {
                      HistoryManager.gotoSIARDInfo(databaseUUID);
                    }
                  }
                });
            } else {
              BrowserService.Util.getInstance().uploadMetadataSIARD(path, new DefaultAsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                  // TODO: error handling
                }

                @Override
                public void onSuccess(String newDatabaseUUID) {
                  HistoryManager.gotoSIARDInfo(newDatabaseUUID);
                }
              });
            }
          }
        });
      }
    });

    Button btnManage = new Button();
    btnManage.setText(messages.manageCardButton());

    btnManage.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        HistoryManager.gotoDatabaseList();
      }
    });

    Card createCard = Card.createInstance(messages.createCardHeader(), messages.createCardText(), btnCreate);
    Card openCard = Card.createInstance(messages.openCardHeader(), messages.openCardText(), btnOpen);
    Card manageCard = Card.createInstance(messages.manageCardHeader(), messages.manageCardText(), btnManage);

    options.add(createCard);
    options.add(openCard);
    options.add(manageCard);
  }
}