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
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.TooltipTextColumn;
import org.roda.wui.client.common.utils.StringUtils;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class IngestJobReportList extends AsyncTableCell<IndexedReport> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TooltipTextColumn<IndexedReport> sourceObjectColumn;
  private TooltipTextColumn<IndexedReport> outcomeObjectColumn;
  private Column<IndexedReport, Date> updatedDateColumn;
  private Column<IndexedReport, SafeHtml> lastPluginRunStateColumn;
  private TextColumn<IndexedReport> completionStatusColumn;
  private TextColumn<IndexedReport> failedCountColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.JOB_REPORT_ID, RodaConstants.JOB_REPORT_JOB_ID, RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS,
    RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL, RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME,
    RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL, RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID,
    RodaConstants.JOB_REPORT_DATE_UPDATED, RodaConstants.JOB_REPORT_PLUGIN, RodaConstants.JOB_REPORT_PLUGIN_VERSION,
    RodaConstants.JOB_REPORT_PLUGIN_STATE, RodaConstants.JOB_REPORT_STEPS_COMPLETED,
    RodaConstants.JOB_REPORT_TOTAL_STEPS, RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE,
    RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedReport> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }


  @Override
  protected void configureDisplay(CellTable<IndexedReport> display) {

    sourceObjectColumn = new TooltipTextColumn<IndexedReport>() {
      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          if (report.getSourceObjectOriginalIds().isEmpty()) {
            value = report.getSourceObjectId();
          } else {
            value = StringUtils.prettyPrint(report.getSourceObjectOriginalIds());
          }

          value = report.getSourceObjectOriginalName() + " (" + value + ")";
        }

        return value;
      }
    };

    outcomeObjectColumn = new TooltipTextColumn<IndexedReport>() {

      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          if (StringUtils.isNotBlank(report.getOutcomeObjectLabel())) {
            value = report.getOutcomeObjectLabel() + " (" + report.getOutcomeObjectId() + ")";
          } else if (StringUtils.isNotBlank(report.getOutcomeObjectId())) {
            value = report.getOutcomeObjectId();
          }
        }

        return value;
      }
    };

    updatedDateColumn = new Column<IndexedReport, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT))) {
      @Override
      public Date getValue(IndexedReport report) {
        return report != null ? report.getDateUpdated() : null;
      }
    };

    lastPluginRunStateColumn = new Column<IndexedReport, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedReport report) {
        SafeHtml ret = null;
        if (report != null) {
          switch (report.getPluginState()) {
            case SUCCESS:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-success'>" + messages.pluginStateMessage(PluginState.SUCCESS) + "</span>");
              break;
            case RUNNING:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-default'>" + messages.pluginStateMessage(PluginState.RUNNING) + "</span>");
              break;
            case FAILURE:
            default:
              ret = SafeHtmlUtils.fromSafeConstant(
                "<span class='label-danger'>" + messages.pluginStateMessage(PluginState.FAILURE) + "</span>");
              break;
          }
        }

        return ret;
      }
    };

    completionStatusColumn = new TextColumn<IndexedReport>() {

      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          value = report.getStepsCompleted() + " " + messages.of() + " " + report.getTotalSteps() + " ("
            + report.getCompletionPercentage() + "%)";
        }

        return value;
      }
    };

    failedCountColumn = new TextColumn<IndexedReport>() {

      @Override
      public String getValue(IndexedReport report) {
        String value = "";
        if (report != null) {
          value = Integer.toString(report.getUnsuccessfulPluginsCounter());
        }

        return value;
      }
    };

    sourceObjectColumn.setSortable(true);
    outcomeObjectColumn.setSortable(true);
    updatedDateColumn.setSortable(true);
    lastPluginRunStateColumn.setSortable(true);
    completionStatusColumn.setSortable(false);
    failedCountColumn.setSortable(true);

    addColumn(sourceObjectColumn, messages.showSIPExtended(), true, false);
    addColumn(outcomeObjectColumn, messages.showAIPExtended(), true, false);
    addColumn(updatedDateColumn, messages.reportLastUpdatedAt(), true, false, 11);
    addColumn(lastPluginRunStateColumn, messages.reportStatus(), true, false, 8);
    addColumn(completionStatusColumn, messages.reportProgress(), true, false, 8);
    addColumn(failedCountColumn, messages.reportFailed(), true, false, 6);

    Label emptyInfo = new Label(messages.noItemsToDisplay());
    display.setEmptyTableWidget(emptyInfo);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(updatedDateColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedReport, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(sourceObjectColumn, Arrays.asList(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID));
    columnSortingKeyMap.put(outcomeObjectColumn, Arrays.asList(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID));
    columnSortingKeyMap.put(updatedDateColumn, Arrays.asList(RodaConstants.JOB_REPORT_DATE_UPDATED));
    columnSortingKeyMap.put(lastPluginRunStateColumn, Arrays.asList(RodaConstants.JOB_REPORT_PLUGIN_STATE));
    columnSortingKeyMap.put(failedCountColumn, Arrays.asList(RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS_COUNTER));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
