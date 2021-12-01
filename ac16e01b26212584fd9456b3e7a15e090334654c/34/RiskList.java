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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class RiskList extends AsyncTableCell<IndexedRisk> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedRisk> nameColumn;
  private Column<IndexedRisk, Date> identifiedOnColumn;
  private TextColumn<IndexedRisk> ownerColumn;
  private Column<IndexedRisk, SafeHtml> severityColumn;
  private TextColumn<IndexedRisk> incidenceCounterColumn;
  private TextColumn<IndexedRisk> notMitigatedIncidenceCounterColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_ID,
    RodaConstants.RISK_NAME, RodaConstants.RISK_IDENTIFIED_ON, RodaConstants.RISK_CATEGORIES,
    RodaConstants.RISK_MITIGATION_OWNER, RodaConstants.RISK_CURRENT_SEVERITY_LEVEL, RodaConstants.RISK_INCIDENCES_COUNT,
    RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT, RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL,
    RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedRisk> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }


  @Override
  protected void configureDisplay(CellTable<IndexedRisk> display) {

    nameColumn = new TextColumn<IndexedRisk>() {
      @Override
      public String getValue(IndexedRisk risk) {
        return risk != null ? risk.getName() : null;
      }
    };

    identifiedOnColumn = new Column<IndexedRisk, Date>(
      new DateCell(DateTimeFormat.getFormat(RodaConstants.SIMPLE_DATE_FORMATTER))) {
      @Override
      public Date getValue(IndexedRisk risk) {
        return risk != null ? risk.getIdentifiedOn() : null;
      }
    };

    ownerColumn = new TextColumn<IndexedRisk>() {
      @Override
      public String getValue(IndexedRisk risk) {
        return risk != null ? risk.getMitigationOwner() : null;
      }
    };

    severityColumn = new Column<IndexedRisk, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedRisk risk) {
        SafeHtml ret = null;
        if (risk != null) {
          ret = HtmlSnippetUtils.getSeverityDefinition(risk.getCurrentSeverityLevel());
        }

        return ret;
      }
    };

    incidenceCounterColumn = new TextColumn<IndexedRisk>() {
      @Override
      public String getValue(IndexedRisk risk) {
        return Integer.toString(risk.getIncidencesCount());
      }
    };

    notMitigatedIncidenceCounterColumn = new TextColumn<IndexedRisk>() {
      @Override
      public String getValue(IndexedRisk risk) {
        return Integer.toString(risk.getUnmitigatedIncidencesCount());
      }
    };

    nameColumn.setSortable(true);
    identifiedOnColumn.setSortable(true);
    ownerColumn.setSortable(true);
    severityColumn.setSortable(true);
    incidenceCounterColumn.setSortable(true);
    notMitigatedIncidenceCounterColumn.setSortable(true);

    addColumn(nameColumn, messages.riskName(), false, false);
    addColumn(ownerColumn, messages.riskMitigationOwner(), false, false);
    addColumn(identifiedOnColumn, messages.riskIdentifiedOn(), false, false, 8);
    addColumn(severityColumn, messages.riskPostMitigationSeverity(), false, false, 7);
    addColumn(incidenceCounterColumn, messages.riskIncidences(), false, false, 6);
    addColumn(notMitigatedIncidenceCounterColumn, messages.riskNotMitigatedIncidences(), false, false, 6);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(severityColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedRisk, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.RISK_NAME));
    columnSortingKeyMap.put(identifiedOnColumn, Arrays.asList(RodaConstants.RISK_IDENTIFIED_ON));
    columnSortingKeyMap.put(ownerColumn, Arrays.asList(RodaConstants.RISK_MITIGATION_OWNER));
    columnSortingKeyMap.put(severityColumn, Arrays.asList(RodaConstants.RISK_CURRENT_SEVERITY_LEVEL));
    columnSortingKeyMap.put(incidenceCounterColumn, Arrays.asList(RodaConstants.RISK_INCIDENCES_COUNT));
    columnSortingKeyMap.put(notMitigatedIncidenceCounterColumn,
      Arrays.asList(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

  @Override
  protected ProvidesKey<IndexedRisk> getKeyProvider() {
    return new ProvidesKey<IndexedRisk>() {
      @Override
      public Object getKey(IndexedRisk item) {
        return item.getId();
      }
    };
  }
}
