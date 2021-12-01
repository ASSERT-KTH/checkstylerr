/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.ingest.transfer;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectTransferResourceDialog;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.search.TransferredResourceSearch;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class IngestTransfer extends Composite {

  @SuppressWarnings("unused")
  private static final String TRANSFERRED_RESOURCE_ID_SEPARATOR = "/";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "transfer";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestTransfer instance = null;

  private static final Filter DEFAULT_FILTER = new Filter(
    new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID));

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.TRANSFERRED_RESOURCE_NAME, RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID,
    RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID, RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH,
    RodaConstants.TRANSFERRED_RESOURCE_SIZE, RodaConstants.TRANSFERRED_RESOURCE_DATE,
    RodaConstants.TRANSFERRED_RESOURCE_ISFILE);

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private TransferredResource resource;

  @UiField
  Label ingestTransferTitle;

  @UiField
  FlowPanel ingestTransferDescription;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  TransferredResourceSearch resourceSearch;

  @UiField
  Button download;

  @UiField
  Label lastScanned;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  // BUTTONS
  @UiField
  Button refresh;

  @UiField
  Button uploadFiles;

  @UiField
  Button createFolder;

  @UiField
  Button remove;

  @UiField
  Button startIngest;

  @UiField
  Button rename;

  @UiField
  Button move;

  private IngestTransfer() {
    resourceSearch = new TransferredResourceSearch("IngestTransfer_transferredResources");
    resourceSearch.defaultFilters(new Filter(new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)));

    initWidget(uiBinder.createAndBindUi(this));

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    resourceSearch.getList().addValueChangeHandler(new ValueChangeHandler<IndexResult<TransferredResource>>() {
      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<TransferredResource>> event) {
        updateVisibles();
      }
    });

    resourceSearch.getList().addCheckboxSelectionListener(new CheckboxSelectionListener<TransferredResource>() {
      @Override
      public void onSelectionChange(SelectedItems<TransferredResource> selected) {
        boolean empty = ClientSelectedItemsUtils.isEmpty(selected);

        remove.setText(empty ? messages.removeWholeFolderButton() : messages.removeSelectedItemsButton());
        startIngest.setText(empty ? messages.ingestWholeFolderButton() : messages.ingestSelectedItemsButton());
        updateVisibles();

        if (selected instanceof SelectedItemsList) {
          SelectedItemsList<TransferredResource> selectedList = (SelectedItemsList<TransferredResource>) selected;
          int size = selectedList.getIds().size();
          move.setEnabled(size > 0);
          rename.setEnabled(size == 1 || (size == 0 && resource != null));
        } else if (selected instanceof SelectedItemsFilter) {
          move.setEnabled(true);
          rename.setEnabled(false);
        }
      }
    });

    rename.setEnabled(resource != null);
    move.setEnabled(false);
  }

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static IngestTransfer getInstance() {
    if (instance == null) {
      instance = new IngestTransfer();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  protected void view(TransferredResource r) {
    resource = r;

    ingestTransferTitle.setVisible(false);
    ingestTransferDescription.setVisible(false);

    HTML itemIconHtmlPanel = new HTML(
      r.isFile() ? DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FILE, false)
        : DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(r.getName());
    itemDates.setText(messages.ingestTransferItemInfo(Humanize.formatDateTime(r.getCreationDate()),
      Humanize.readableFileSize(r.getSize())));
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");

    if (r.isFile()) {
      resourceSearch.setVisible(false);
      download.setVisible(true);
      move.setEnabled(true);
    } else {
      Filter filter = new Filter(
        new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, r.getRelativePath()));
      resourceSearch.defaultFilters(filter);
      resourceSearch.setVisible(true);
      download.setVisible(false);
    }

    move.setEnabled(resource != null);
    rename.setEnabled(resource != null);
    breadcrumb.updatePath(BreadcrumbUtils.getTransferredResourceBreadcrumbs(r));
    breadcrumb.setVisible(true);

    lastScanned.setText(messages.ingestTransferLastScanned(resource.getLastScanDate()));
    refresh.setTitle(messages.ingestTransferLastScanned(resource.getLastScanDate()));

    updateVisibles();
  }

  protected void view() {
    resource = null;

    ingestTransferTitle.setVisible(true);
    ingestTransferDescription.setVisible(true);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(messages.ingestAllTransferredPackages());
    itemDates.setText("");
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    move.setEnabled(resource != null);
    rename.setEnabled(resource != null);
    resourceSearch.setVisible(true);
    download.setVisible(false);

    resourceSearch.defaultFilters(DEFAULT_FILTER);
    breadcrumb.setVisible(false);

    lastScanned.setText("");
    refresh.setTitle("");

    updateVisibles();
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      view();
      callback.onSuccess(this);
    } else if (!historyTokens.isEmpty()
      && historyTokens.get(0).equals(TransferUpload.INGEST_RESOLVER.getHistoryToken())) {
      TransferUpload.INGEST_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      String transferredResourceUUID = historyTokens.get(0);
      if (transferredResourceUUID != null) {
        BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), transferredResourceUUID,
          fieldsToReturn, new AsyncCallback<TransferredResource>() {

            @Override
            public void onFailure(Throwable caught) {
              if (caught instanceof NotFoundException) {
                Dialogs.showInformationDialog(messages.ingestTransferNotFoundDialogTitle(),
                  messages.ingestTransferNotFoundDialogMessage(), messages.ingestTransferNotFoundDialogButton(), false,
                  new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(Void result) {
                      HistoryUtils.newHistory(IngestTransfer.RESOLVER);
                    }
                  });
              } else {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
                HistoryUtils.newHistory(IngestTransfer.RESOLVER);
              }

              callback.onSuccess(null);
            }

            @Override
            public void onSuccess(TransferredResource r) {
              view(r);
              callback.onSuccess(IngestTransfer.this);
            }
          });
      } else {
        view();
        callback.onSuccess(this);
      }

    }

  }

  protected void updateVisibles() {
    uploadFiles.setEnabled(resource == null || !resource.isFile());
    createFolder.setEnabled(resource == null || !resource.isFile());
    boolean empty = ClientSelectedItemsUtils.isEmpty(resourceSearch.getSelected());
    remove.setEnabled(resource != null || !empty);
    startIngest.setEnabled(resource != null || !empty);
  }

  @UiHandler("refresh")
  void buttonRefreshHandler(ClickEvent e) {
    String relativePath = resource != null ? resource.getRelativePath() : null;
    refresh.setEnabled(false);

    BrowserService.Util.getInstance().transferScanRequestUpdate(relativePath, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof IsStillUpdatingException) {
          Toast.showInfo(messages.dialogRefresh(), messages.updateIsCurrentlyRunning());
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        resourceSearch.refresh();
        refresh.setEnabled(true);
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo(messages.dialogRefresh(), messages.updatedFilesUnderFolder());
        resourceSearch.refresh();
        refresh.setEnabled(true);
      }
    });
  }

  @UiHandler("uploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {
    if (resource != null) {
      HistoryUtils.newHistory(TransferUpload.INGEST_RESOLVER, resource.getUUID());
    } else {
      HistoryUtils.newHistory(TransferUpload.INGEST_RESOLVER);
    }
  }

  @UiHandler("createFolder")
  void buttonCreateFolderHandler(ClickEvent e) {
    Dialogs.showPromptDialog(messages.ingestTransferCreateFolderTitle(), messages.ingestTransferCreateFolderMessage(),
      null, null, RegExp.compile("^[^/]+$"), messages.dialogCancel(), messages.dialogOk(), true, false,
      new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String folderName) {
          String parent = resource != null ? resource.getUUID() : null;
          BrowserService.Util.getInstance().createTransferredResourcesFolder(parent, folderName, true,
            new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(String newResourceUUID) {
                HistoryUtils.newHistory(RESOLVER, newResourceUUID);
              }
            });
        }
      });
  }

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {
    final SelectedItems<TransferredResource> selected = resourceSearch.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      // Remove the whole folder

      if (resource != null) {
        Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
          messages.ingestTransferRemoveFolderConfirmDialogMessage(resource.getName()), messages.dialogNo(),
          messages.dialogYes(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                SelectedItems<TransferredResource> s = new SelectedItemsList<>(Arrays.asList(resource.getUUID()),
                  TransferredResource.class.getName());
                BrowserService.Util.getInstance().deleteTransferredResources(s, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.removeSuccessTitle(), messages.removeSuccessMessage(1L));
                    HistoryUtils.newHistory(RESOLVER, resource.getParentUUID());
                  }
                });
              }
            }
          });
      }
      // else do nothing

    } else {
      // Remove all selected resources
      ClientSelectedItemsUtils.size(TransferredResource.class, selected, new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final Long size) {
          Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
            messages.ingestTransferRemoveSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
            new AsyncCallback<Boolean>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Boolean confirmed) {
                if (confirmed) {
                  BrowserService.Util.getInstance().deleteTransferredResources(selected, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      AsyncCallbackUtils.defaultFailureTreatment(caught);
                      resourceSearch.refresh();
                    }

                    @Override
                    public void onSuccess(Void result) {
                      Toast.showInfo(messages.removeSuccessTitle(), messages.removeSuccessMessage(size));
                      resourceSearch.refresh();
                      move.setEnabled(false);
                      rename.setEnabled(false);
                    }
                  });
                }
              }
            });
        }
      });

    }

  }

  @UiHandler("startIngest")
  void buttonStartIngestHandler(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    selectedItems.setSelectedItems(getSelected());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_INGEST);
  }

  public SelectedItems<TransferredResource> getSelected() {
    SelectedItems<TransferredResource> selected = resourceSearch.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected) && resource != null) {
      selected = new SelectedItemsList<>(Arrays.asList(resource.getUUID()), TransferredResource.class.getName());
    }

    return selected;
  }

  @UiHandler("download")
  public void handleDownload(ClickEvent e) {
    if (resource != null) {
      SafeUri downloadUri = RestUtils.createTransferredResourceDownloadUri(resource.getUUID());
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("rename")
  void buttonRenameHandler(ClickEvent e) {
    if (!ClientSelectedItemsUtils.isEmpty(getSelected()) && getSelected() instanceof SelectedItemsList) {
      SelectedItemsList<TransferredResource> resourceList = (SelectedItemsList<TransferredResource>) getSelected();

      BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), resourceList.getIds().get(0),
        Arrays.asList(RodaConstants.TRANSFERRED_RESOURCE_ID, RodaConstants.TRANSFERRED_RESOURCE_NAME),
        new AsyncCallback<TransferredResource>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
          }

          @Override
          public void onSuccess(final TransferredResource resultResource) {
            Dialogs.showPromptDialog(messages.renameTransferredResourcesDialogTitle(), null, resultResource.getName(),
              null, RegExp.compile("^[^/]*$"), messages.cancelButton(), messages.confirmButton(), true, false,
              new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String result) {
                  BrowserService.Util.getInstance().renameTransferredResource(resultResource.getUUID(), result,
                    new AsyncCallback<String>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
                      }

                      @Override
                      public void onSuccess(String result) {
                        Toast.showInfo(messages.dialogSuccess(), messages.renameSIPSuccessful());
                        HistoryUtils.newHistory(IngestTransfer.RESOLVER, result);
                      }
                    });
                }
              });
          }
        });
    } else {
      return;
    }
  }

  @UiHandler("move")
  void buttonMoveHandler(ClickEvent e) {
    SelectedItems<TransferredResource> selected = getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected) && resource != null) {
      selected = SelectedItemsList.create(TransferredResource.class.getName(), resource.getUUID());
    }

    BrowserService.Util.getInstance().retrieveSelectedTransferredResource(selected,
      new AsyncCallback<List<TransferredResource>>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showInfo(messages.dialogFailure(), messages.moveSIPFailed());
        }

        @Override
        public void onSuccess(List<TransferredResource> result) {
          doTransferredResourceMove(result);
        }
      });
  }

  private void doTransferredResourceMove(List<TransferredResource> resources) {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, Boolean.FALSE.toString()));

    if (resource != null && resource.isFile()) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, resources.get(0).getParentUUID()));
    }

    SelectTransferResourceDialog dialog = new SelectTransferResourceDialog(messages.selectParentTitle(), filter);
    if (resources.size() <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
      dialog.addStyleName("object-dialog");
    }
    dialog.setEmptyParentButtonVisible(true);
    dialog.showAndCenter();
    dialog.addValueChangeHandler(new ValueChangeHandler<TransferredResource>() {

      @Override
      public void onValueChange(ValueChangeEvent<TransferredResource> event) {
        final TransferredResource transferredResource = event.getValue();

        BrowserService.Util.getInstance().moveTransferredResource(getSelected(), transferredResource,
          new AsyncCallback<Job>() {

            @Override
            public void onFailure(Throwable caught) {
              Toast.showInfo(messages.dialogFailure(), messages.moveSIPFailed());
              HistoryUtils.newHistory(InternalProcess.RESOLVER);
            }

            @Override
            public void onSuccess(Job result) {
              Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                  Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                  Timer timer = new Timer() {
                    @Override
                    public void run() {
                      if (transferredResource.getUUID() != null) {
                        HistoryUtils.newHistory(IngestTransfer.RESOLVER, transferredResource.getUUID());
                      } else {
                        HistoryUtils.newHistory(IngestTransfer.RESOLVER);
                      }
                    }
                  };

                  timer.schedule(RodaConstants.ACTION_TIMEOUT);
                }

                @Override
                public void onSuccess(final Void nothing) {
                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                }
              });
            }
          });
      }
    });
  }
}
