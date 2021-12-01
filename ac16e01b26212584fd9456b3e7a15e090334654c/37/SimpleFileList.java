/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class SimpleFileList extends AsyncTableCell<IndexedFile> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<IndexedFile, SafeHtml> iconColumn;
  private TextColumn<IndexedFile> filenameColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_AIP_ID,
    RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_PATH, RodaConstants.FILE_ISDIRECTORY,
    RodaConstants.FILE_REPRESENTATION_UUID, RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_FILE_ID,
    RodaConstants.FILE_SIZE, RodaConstants.FILE_FORMAT_VERSION, RodaConstants.FILE_FILEFORMAT);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedFile> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }


  @Override
  protected void configureDisplay(CellTable<IndexedFile> display) {
    iconColumn = new Column<IndexedFile, SafeHtml>(new SafeHtmlCell()) {

      @Override
      public SafeHtml getValue(IndexedFile file) {
        if (file != null) {
          if (file.isDirectory()) {
            return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
          } else {
            return SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");
          }
        } else {
          logger.error("Trying to display a NULL item");
        }
        return null;
      }
    };

    filenameColumn = new TextColumn<IndexedFile>() {

      @Override
      public String getValue(IndexedFile file) {
        String fileName = null;
        if (file != null) {
          fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
        }

        return fileName;
      }
    };

    /* add sortable */
    iconColumn.setSortable(true);
    filenameColumn.setSortable(true);

    addColumn(iconColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-files-o'></i>"), false, false, 2);
    addColumn(filenameColumn, messages.fileName(), false, false);

    display.setColumnWidth(iconColumn, 2.5, Unit.EM);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(filenameColumn, false));
    addStyleName("my-files-table");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedFile, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(iconColumn, Arrays.asList(RodaConstants.FILE_ISDIRECTORY));
    columnSortingKeyMap.put(filenameColumn, Arrays.asList(RodaConstants.FILE_ORIGINALNAME));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
