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

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils.ProcessRelativeItem;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseDIP extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveViewersProperties(new AsyncCallback<Viewers>() {

        @Override
        public void onSuccess(Viewers viewers) {
          load(viewers, historyTokens, callback);
        }

        @Override
        public void onFailure(Throwable caught) {
          errorRedirect(callback);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "dip";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        final String historyDipUUID = historyTokens.get(0);
        final String historyDipFileUUID = historyTokens.size() > 1 ? historyTokens.get(1) : null;

        BrowserService.Util.getInstance().retrieveDipBundle(historyDipUUID, historyDipFileUUID,
          new AsyncCallback<DipBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(DipBundle dipBundle) {
              callback.onSuccess(new BrowseDIP(viewers, dipBundle));
            }
          });

      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
      callback.onSuccess(null);
    }

  };

  interface MyUiBinder extends UiBinder<Widget, BrowseDIP> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final Sorter DEFAULT_DIPFILE_SORTER = new Sorter(new SortParameter(RodaConstants.DIPFILE_ID, false));

  // system
  private final Viewers viewers;

  // source
  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private IndexedFile file;

  // target
  private IndexedDIP dip;
  private DIPFile dipFile;
  private List<DIPFile> dipFileAncestors;

  private ClientLogger logger = new ClientLogger(getClass().getName());

  // interface

  @UiField
  AccessibleFocusPanel keyboardFocus;

  @UiField
  FlowPanel refererToolbar;

  @UiField
  Label refererTitle;

  @UiField
  BreadcrumbPanel refererBreadcrumb, breadcrumb;

  @UiField
  FlowPanel center;

  @UiField
  AccessibleFocusPanel disseminationsButton, refererPreviousButton, refererNextButton, refererInfoButton,
    refererOptionsButton;

  @UiField
  AccessibleFocusPanel previousButton, nextButton, dipOptionsButton;

  public BrowseDIP(Viewers viewers, DipBundle bundle) {
    this.viewers = viewers;

    this.aip = bundle.getAip();
    this.representation = bundle.getRepresentation();
    this.file = bundle.getFile();

    this.dip = bundle.getDip();

    this.dipFile = bundle.getDipFile();
    this.dipFileAncestors = bundle.getDipFileAncestors();

    initWidget(uiBinder.createAndBindUi(this));

    // TODO set title for previous and next button
    HtmlSnippetUtils.setCssClassDisabled(previousButton, true);
    HtmlSnippetUtils.setCssClassDisabled(nextButton, true);

    update();

    initializeRefererListSelectionState();
    if (dipFile != null) {
      ListSelectionUtils.bindLayout(dipFile, previousButton, nextButton, keyboardFocus, true, false, false);
    }

    keyboardFocus.setFocus(true);

    if (file != null) {
      Sliders.createDisseminationsSlider(center, disseminationsButton, file);
      Sliders.createInfoSlider(center, refererInfoButton, file);
      Sliders.createOptionsSlider(center, refererOptionsButton, file);
    } else if (representation != null) {
      Sliders.createDisseminationsSlider(center, disseminationsButton, representation);
      Sliders.createInfoSlider(center, refererInfoButton, representation);
      Sliders.createOptionsSlider(center, refererOptionsButton, representation);
    } else if (aip != null) {
      Sliders.createDisseminationsSlider(center, disseminationsButton, aip);
      Sliders.createInfoSlider(center, refererInfoButton, aip);
      refererOptionsButton.setVisible(false);
    } else {
      disseminationsButton.setVisible(false);
      refererInfoButton.setVisible(false);
      refererOptionsButton.setVisible(false);
    }

    if (dipFile != null) {
      Sliders.createOptionsSlider(center, dipOptionsButton, dipFile);
    } else {
      Sliders.createOptionsSlider(center, dipOptionsButton, dip);
    }
  }

  private void initializeRefererListSelectionState() {
    boolean requireCtrlModifier = true;
    boolean requireShiftModifier = true;
    boolean requireAltModifier = false;

    if (aip != null && representation != null && file != null) {
      refererTitle.setText(file.isDirectory() ? messages.catalogueFolderTitle() : messages.catalogueFileTitle());
      refererBreadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(aip, representation, file));
      refererBreadcrumb.setVisible(true);
      ListSelectionUtils.bindLayout(file, refererPreviousButton, refererNextButton, keyboardFocus, requireCtrlModifier,
        requireShiftModifier, requireAltModifier, new ProcessRelativeItem<IndexedFile>() {

          @Override
          public void process(final IndexedFile file) {
            // find DIP for this file
            Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, file.getUUID()));
            openReferred(file, filter);
          }
        });
    } else if (aip != null && representation != null) {
      refererTitle.setText(messages.catalogueRepresentationTitle());
      refererBreadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(aip, representation));
      refererBreadcrumb.setVisible(true);
      ListSelectionUtils.bindLayout(representation, refererPreviousButton, refererNextButton, keyboardFocus,
        requireCtrlModifier, requireShiftModifier, requireAltModifier,
        new ProcessRelativeItem<IndexedRepresentation>() {

          @Override
          public void process(final IndexedRepresentation representation) {
            // find DIP for this file
            Filter filter = new Filter(
              new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, representation.getUUID()));
            openReferred(representation, filter);
          }
        });
    } else if (aip != null) {
      refererTitle.setText(messages.catalogueItemTitle());
      refererBreadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(aip));
      refererBreadcrumb.setVisible(true);
      ListSelectionUtils.bindLayout(aip, refererPreviousButton, refererNextButton, keyboardFocus, requireCtrlModifier,
        requireShiftModifier, requireAltModifier, new ProcessRelativeItem<IndexedAIP>() {

          @Override
          public void process(final IndexedAIP aip) {
            // find DIP for this file
            Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getUUID()));
            openReferred(aip, filter);
          }
        });
    } else

    {
      refererToolbar.setVisible(false);
    }
  }

  private static <T extends IsIndexed> void openReferred(final T object, Filter filter) {
    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, DEFAULT_DIPFILE_SORTER,
      new Sublist(0, 1), Facets.NONE, LocaleInfo.getCurrentLocale().getLocaleName(), true,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID), new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          if (result.getTotalCount() > 0) {
            // open DIP
            HistoryUtils.openBrowse(result.getResults().get(0));
          } else {
            // open object
            HistoryUtils.resolve(object);
          }
        }
      });
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.smoothScroll(breadcrumb.getElement());
  }

  private void update() {
    if (dipFile != null) {
      center.add(new DipFilePreview(viewers, dipFile));
    } else if (dip.getOpenExternalURL() != null) {
      center.add(new DipUrlPreview(viewers, dip));
    } else {
      final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()),
        new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));

      ListBuilder<DIPFile> dipFileListBuilder = new ListBuilder<>(DIPFileList::new,
        new AsyncTableCellOptions<>(DIPFile.class, "BrowseDIP_dipFiles").withFilter(filter)
          .withSummary(messages.allOfAObject(DIPFile.class.getName())).bindOpener());

      SearchWrapper search = new SearchWrapper(false).createListAndSearchPanel(dipFileListBuilder);

      SimplePanel layout = new SimplePanel();
      layout.add(search);
      center.add(layout);
      layout.addStyleName("browseDip-topList");
    }

    // update breadcrumb
    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);
  }

  public void open(String parentDipDileUUID, final Sorter sorter, final int openIndex) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()));

    if (parentDipDileUUID == null) {
      filter.add(new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));
    } else {
      filter.add(new SimpleFilterParameter(RodaConstants.DIPFILE_PARENT_UUID, parentDipDileUUID));
    }

    Sublist sublist = new Sublist(openIndex, 1);
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(DIPFile.class.getName(), filter, sorter, sublist, Facets.NONE, localeString,
      justActive, Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIPFILE_DIP_ID, RodaConstants.DIPFILE_ID),
      new AsyncCallback<IndexResult<DIPFile>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<DIPFile> result) {
          if (!result.getResults().isEmpty()) {
            DIPFile firstDipFile = result.getResults().get(0);
            HistoryUtils.openBrowse(firstDipFile);
          } else {
            Toast.showError("No files in the DIP");
            // TODO better handle this case
          }
        }
      });
  }

  private List<BreadcrumbItem> getBreadcrumbs() {
    return BreadcrumbUtils.getDipBreadcrumbs(dip, dipFile, dipFileAncestors);
  }

}
