/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.common.actions.AbstractActionable;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.ActionableObject;
import org.roda.wui.client.common.actions.ActionableWidgetBuilder;
import org.roda.wui.client.common.actions.ActionsBundle;
import org.roda.wui.client.common.actions.ActionsGroup;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class NavigationToolbar<T extends IsIndexed> extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, NavigationToolbar> {
  }

  @UiField
  AccessibleFocusPanel keyboardFocus;

  // breadcrumb on left side

  @UiField
  Label navigationToolbarHeader;

  @UiField
  BreadcrumbPanel breadcrumb;

  // buttons on the right side

  @UiField
  AccessibleFocusPanel searchButton;

  @UiField
  AccessibleFocusPanel previousButton;

  @UiField
  AccessibleFocusPanel nextButton;

  @UiField
  AccessibleFocusPanel sidebarButton;

  private T currentObject = null;
  private HandlerRegistration searchPopupClickHandler = null;

  public NavigationToolbar() {
    initWidget(uiBinder.createAndBindUi(this));
    refresh();
  }

  public void setObject(T object) {
    currentObject = object;
    refresh();
  }

  public AccessibleFocusPanel getSidebarButton() {
    return sidebarButton;
  }

  public void setHeader(String headerText) {
    navigationToolbarHeader.setText(headerText);
  }

  public void hide() {
    this.addStyleName("navigationToolbar-hidden");
  }

  public void show() {
    this.removeStyleName("navigationToolbar-hidden");
  }

  public void refresh() {
    ListSelectionUtils.bindLayout(currentObject, previousButton, nextButton, keyboardFocus, true, false, false);
    setupSearchPopup();
  }

  // Breadcrumb management

  public void clearBreadcrumb() {
    breadcrumb.clear();
  }

  public void updateBreadcrumb(BrowseAIPBundle bundle) {
    breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(bundle.getAIPAncestors(), bundle.getAip()));
  }

  public void updateBreadcrumbPath(BreadcrumbItem... items) {
    updateBreadcrumbPath(Arrays.asList(items));
  }

  public void updateBreadcrumbPath(List<BreadcrumbItem> items) {
    breadcrumb.updatePath(items);
  }

  private void setupSearchPopup() {
    clearSearchPopupHandlers();
    if (currentObject instanceof IndexedAIP) {
      SearchPopup popup = new SearchPopup((IndexedAIP) currentObject);
      searchPopupClickHandler = searchButton
        .addClickHandler(event -> popup.showRelativeTo(searchButton, CalloutPopup.CalloutPosition.TOP_RIGHT));
      searchButton.setVisible(true);
    } else {
      searchButton.setVisible(false);
    }
  }

  private void clearSearchPopupHandlers() {
    if (searchPopupClickHandler != null) {
      searchPopupClickHandler.removeHandler();
    }
    searchPopupClickHandler = null;
  }

  private static class SearchPopup extends CalloutPopup {
    public SearchPopup(IndexedAIP aip) {
      super();
      setWidget(
        new ActionableWidgetBuilder<>(SearchAipActions.get()).buildListWithObjects(new ActionableObject<>(aip)));
    }
  }

  private static class SearchAipActions extends AbstractActionable<IndexedAIP> {
    private static final SearchAipActions INSTANCE = new SearchAipActions();

    private static final Set<SearchAipAction> POSSIBLE_ACTIONS = new HashSet<>(Arrays.asList(SearchAipAction.values()));

    private SearchAipActions() {
      // do nothing
    }

    public enum SearchAipAction implements Actionable.Action<IndexedAIP> {
      SEARCH_DESCENDANTS, SEARCH_PACKAGE
    }

    public static SearchAipActions get() {
      return INSTANCE;
    }

    @Override
    public boolean canAct(Action<IndexedAIP> action, IndexedAIP object) {
      return POSSIBLE_ACTIONS.contains(action);
    }

    @Override
    public void act(Action<IndexedAIP> action, IndexedAIP object, AsyncCallback<ActionImpact> callback) {
      if (action.equals(SearchAipAction.SEARCH_DESCENDANTS)) {
        HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_ITEMS, RodaConstants.AIP_ANCESTORS,
          object.getId());
      } else if (action.equals(SearchAipAction.SEARCH_PACKAGE)) {
        HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_REPRESENTATIONS,
          RodaConstants.REPRESENTATION_AIP_ID, object.getId());
      } else {
        unsupportedAction(action, callback);
      }
    }

    @Override
    public ActionsBundle<IndexedAIP> createActionsBundle() {
      ActionsBundle<IndexedAIP> actionsBundle = new ActionsBundle<>();

      // SEARCH
      ActionsGroup<IndexedAIP> searchGroup = new ActionsGroup<>();
      searchGroup.addButton(messages.searchContext(), SearchAipAction.SEARCH_DESCENDANTS, ActionImpact.NONE,
        "btn-sitemap");
      searchGroup.addButton(messages.searchAIP(), SearchAipAction.SEARCH_PACKAGE, ActionImpact.NONE, "btn-archive");

      actionsBundle.addGroup(searchGroup);

      return actionsBundle;
    }

  }
}
