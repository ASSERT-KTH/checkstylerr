package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Cell;
import com.influxdb.client.domain.CellUpdate;
import com.influxdb.client.domain.CreateCell;
import com.influxdb.client.domain.Dashboard;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CellsService {
  /**
   * Delete a dashboard cell
   * 
   * @param dashboardID The ID of the dashboard to delete. (required)
   * @param cellID The ID of the cell to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/cells/{cellID}")
  Call<Void> deleteDashboardsIDCellsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve the view for a cell
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param cellID The cell ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;View&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/cells/{cellID}/view")
  Call<View> getDashboardsIDCellsIDView(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update the non-positional information related to a cell
   * Updates the non positional information related to a cell. Updates to a single cell&#39;s positional data could cause grid conflicts.
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param cellID The ID of the cell to update. (required)
   * @param cellUpdate  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Cell&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}/cells/{cellID}")
  Call<Cell> patchDashboardsIDCellsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Body CellUpdate cellUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update the view for a cell
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param cellID The ID of the cell to update. (required)
   * @param view  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;View&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}/cells/{cellID}/view")
  Call<View> patchDashboardsIDCellsIDView(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Body View view, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a dashboard cell
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param createCell Cell that will be added (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Cell&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/cells")
  Call<Cell> postDashboardsIDCells(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body CreateCell createCell, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Replace cells in a dashboard
   * Replaces all cells in a dashboard. This is used primarily to update the positional information of all cells.
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param cell  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/dashboards/{dashboardID}/cells")
  Call<Dashboard> putDashboardsIDCells(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body List<Cell> cell, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
