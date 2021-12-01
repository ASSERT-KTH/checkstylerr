/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class DIPList extends AsyncTableCell<IndexedDIP> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedDIP> titleColumn;
  private Column<IndexedDIP, Date> dateCreated;
  private Column<IndexedDIP, Date> lastModified;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID,
    RodaConstants.DIP_DATE_CREATED, RodaConstants.DIP_LAST_MODIFIED, RodaConstants.DIP_TITLE);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedDIP> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }


  @Override
  protected void configureDisplay(CellTable<IndexedDIP> display) {

    dateCreated = new Column<IndexedDIP, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(IndexedDIP dip) {
        return dip != null ? dip.getDateCreated() : null;
      }
    };

    lastModified = new Column<IndexedDIP, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(IndexedDIP dip) {
        return dip != null ? dip.getLastModified() : null;
      }
    };

    titleColumn = new TextColumn<IndexedDIP>() {
      @Override
      public String getValue(IndexedDIP dip) {
        return dip != null ? dip.getTitle() : null;
      }
    };

    titleColumn.setSortable(true);
    dateCreated.setSortable(true);
    lastModified.setSortable(true);

    display.addColumn(titleColumn, messages.aipGenericTitle());
    display.addColumn(dateCreated, messages.objectCreatedDate());
    display.addColumn(lastModified, messages.objectLastModified());

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(titleColumn, true));

    display.setColumnWidth(dateCreated, 13.0, Unit.EM);
    display.setColumnWidth(lastModified, 13.0, Unit.EM);

    dateCreated.setCellStyleNames("nowrap");
    lastModified.setCellStyleNames("nowrap");

    addStyleName("my-collections-table");
    emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedDIP, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    // setting secondary sorter to title
    columnSortingKeyMap.put(titleColumn, Arrays.asList(RodaConstants.DIP_TITLE));
    columnSortingKeyMap.put(dateCreated, Arrays.asList(RodaConstants.DIP_DATE_CREATED));
    columnSortingKeyMap.put(lastModified, Arrays.asList(RodaConstants.DIP_LAST_MODIFIED));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
