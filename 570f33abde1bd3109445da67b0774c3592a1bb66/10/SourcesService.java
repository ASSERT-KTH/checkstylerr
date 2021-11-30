package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Buckets;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.Source;
import com.influxdb.client.domain.Sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SourcesService {
  /**
   * Delete a source
   * 
   * @param sourceID The source ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/sources/{sourceID}")
  Call<Void> deleteSourcesID(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all sources
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org The organization name. (optional)
   * @return Call&lt;Sources&gt;
   */
  @GET("api/v2/sources")
  Call<Sources> getSources(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org
  );

  /**
   * Get a source
   * 
   * @param sourceID The source ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Source&gt;
   */
  @GET("api/v2/sources/{sourceID}")
  Call<Source> getSourcesID(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get buckets in a source
   * 
   * @param sourceID The source ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org The organization name. (optional)
   * @return Call&lt;Buckets&gt;
   */
  @GET("api/v2/sources/{sourceID}/buckets")
  Call<Buckets> getSourcesIDBuckets(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org
  );

  /**
   * Get the health of a source
   * 
   * @param sourceID The source ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;HealthCheck&gt;
   */
  @GET("api/v2/sources/{sourceID}/health")
  Call<HealthCheck> getSourcesIDHealth(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a Source
   * 
   * @param sourceID The source ID. (required)
   * @param source Source update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Source&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/sources/{sourceID}")
  Call<Source> patchSourcesID(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Body Source source, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Creates a source
   * 
   * @param source Source to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Source&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/sources")
  Call<Source> postSources(
    @retrofit2.http.Body Source source, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
