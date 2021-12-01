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
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.search.PreservationEventsSearch;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class PreservationEvents extends Composite {

  public static final HistoryResolver BROWSE_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().browseResolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseAIP.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "events";
    }
  };

  public static final HistoryResolver PLANNING_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().planningResolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "events";
    }
  };

  private static PreservationEvents instance = null;

  interface MyUiBinder extends UiBinder<Widget, PreservationEvents> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private static final List<String> aipFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));

  private static final List<String> representationFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID,
      RodaConstants.REPRESENTATION_TYPE));

  private static final List<String> fileFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_PARENT_UUID, RodaConstants.FILE_PATH,
      RodaConstants.FILE_ANCESTORS_PATH, RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_FILE_ID,
      RodaConstants.FILE_AIP_ID, RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_ISDIRECTORY));

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField(provided = true)
  PreservationEventsSearch eventSearch;

  @UiField
  FlowPanel actionsPanel;

  @UiField
  Button downloadButton;

  @UiField
  Button backButton;

  private String aipId;
  private String representationUUID;
  private String fileUUID;

  /**
   * Create a new panel to edit a user
   * 
   * @param itemBundle
   * 
   */
  public PreservationEvents() {
    this(null);
  }

  public PreservationEvents(final String aipId) {
    this(aipId, null);
  }

  public PreservationEvents(final String aipId, final String representationUUID) {
    this(aipId, representationUUID, null);
  }

  public PreservationEvents(final String aipId, final String representationUUID, final String fileUUID) {
    this.aipId = aipId;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;

    Filter filter = new Filter();

    if (aipId != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId));
    }
    if (representationUUID != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representationUUID));
    }
    if (fileUUID != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_FILE_UUID, fileUUID));
    }

    eventSearch = new PreservationEventsSearch("PreservationEvents_events");
    eventSearch.defaultFilters(filter);

    initWidget(uiBinder.createAndBindUi(this));
    actionsPanel.setVisible(aipId != null);

    // create breadcrumbs
    if (fileUUID != null) {
      getFileBreadCrumbs();
    } else if (representationUUID != null) {
      getRepresentationBreadCrumbs();
    } else if (aipId != null) {
      getAIPBreadCrumbs();
    } else {
      breadcrumb.setVisible(false);
    }
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static PreservationEvents getInstance() {
    if (instance == null) {
      instance = new PreservationEvents();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void getAIPBreadCrumbs() {
    BrowserService.Util.getInstance().retrieveBrowseAIPBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
      aipFieldsToReturn, new AsyncCallback<BrowseAIPBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
          HistoryUtils.newHistory(BrowseAIP.RESOLVER);
        }

        @Override
        public void onSuccess(BrowseAIPBundle itemBundle) {
          breadcrumb
            .updatePath(BreadcrumbUtils.getAipBreadcrumbs(itemBundle.getAIPAncestors(), itemBundle.getAip(), true));
          breadcrumb.setVisible(true);
        }
      });
  }

  private void getRepresentationBreadCrumbs() {
    BrowserService.Util.getInstance().retrieve(IndexedRepresentation.class.getName(), representationUUID,
      RodaConstants.REPRESENTATION_FIELDS_TO_RETURN, new AsyncCallback<IndexedRepresentation>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexedRepresentation representation) {
          BrowserService.Util.getInstance().retrieveBrowseRepresentationBundle(representation.getAipId(),
            representation.getId(), LocaleInfo.getCurrentLocale().getLocaleName(), representationFieldsToReturn,
            new AsyncCallback<BrowseRepresentationBundle>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(BrowseRepresentationBundle repBundle) {
                breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(repBundle));
                breadcrumb.setVisible(true);
              }
            });
        }
      });
  }

  private void getFileBreadCrumbs() {
    BrowserService.Util.getInstance().retrieve(IndexedFile.class.getName(), fileUUID,
      RodaConstants.FILE_FIELDS_TO_RETURN, new AsyncCallback<IndexedFile>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexedFile file) {
          BrowserService.Util.getInstance().retrieveBrowseFileBundle(file.getAipId(), file.getRepresentationId(),
            file.getPath(), file.getId(), fileFieldsToReturn, new AsyncCallback<BrowseFileBundle>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(BrowseFileBundle fileBundle) {
                breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(fileBundle));
                breadcrumb.setVisible(true);
              }
            });
        }
      });
  }

  @UiHandler("downloadButton")
  void buttonDownloadHandler(ClickEvent e) {
    if (aipId != null) {
      SafeUri downloadUri = RestUtils.createPreservationMetadataDownloadUri(aipId);
      if (downloadUri != null) {
        Window.Location.assign(downloadUri.asString());
      }
    }
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    if (fileUUID != null) {
      HistoryUtils.newHistory(HistoryUtils.getHistoryUuidResolver(IndexedFile.class.getName(), fileUUID));
    } else if (representationUUID != null) {
      HistoryUtils
        .newHistory(HistoryUtils.getHistoryUuidResolver(IndexedRepresentation.class.getName(), representationUUID));
    } else if (aipId != null) {
      HistoryUtils.newHistory(HistoryUtils.getHistoryBrowse(aipId));
    } else {
      // button not visible
    }
  }

  private void browseResolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      final String aipId = historyTokens.get(0);
      if (aipId.equals(this.aipId) && StringUtils.isBlank(this.representationUUID)) {
        callback.onSuccess(this);
      } else {
        instance = new PreservationEvents(aipId);
        callback.onSuccess(instance);
      }
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(ShowPreservationEvent.RESOLVER.getHistoryToken())) {
      ShowPreservationEvent.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2) {
      final String aipId = historyTokens.get(0);
      final String representationUUID = historyTokens.get(1);

      if (aipId.equals(this.aipId) && representationUUID.equals(this.representationUUID)
        && StringUtils.isBlank(this.fileUUID)) {
        callback.onSuccess(this);
      } else {
        instance = new PreservationEvents(aipId, representationUUID);
        callback.onSuccess(instance);
      }
    } else if (historyTokens.size() == 3) {
      final String aipId = historyTokens.get(0);
      final String representationUUID = historyTokens.get(1);
      final String fileUUID = historyTokens.get(2);

      if (aipId.equals(this.aipId) && representationUUID.equals(this.representationUUID)
        && fileUUID.equals(this.fileUUID)) {
        callback.onSuccess(this);
      } else {
        instance = new PreservationEvents(aipId, representationUUID, fileUUID);
        callback.onSuccess(instance);
      }
    } else {
      HistoryUtils.newHistory(BrowseAIP.RESOLVER);
      callback.onSuccess(null);
    }
  }

  private void planningResolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty() && StringUtils.isBlank(this.aipId)) {
      callback.onSuccess(this);
    } else {
      instance = new PreservationEvents();
      HistoryUtils.newHistory(PLANNING_RESOLVER);
      callback.onSuccess(null);
    }
  }
}
