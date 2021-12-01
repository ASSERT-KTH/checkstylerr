/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.metron.profiler.spark;

import com.google.common.collect.Maps;
import org.apache.metron.common.configuration.profiler.ProfilerConfig;
import org.apache.metron.profiler.MessageRoute;
import org.apache.metron.profiler.ProfileMeasurement;
import org.apache.metron.profiler.spark.function.GroupByPeriodFunction;
import org.apache.metron.profiler.spark.function.HBaseWriterFunction;
import org.apache.metron.profiler.spark.function.MessageRouterFunction;
import org.apache.metron.profiler.spark.function.ProfileBuilderFunction;
import org.apache.metron.profiler.spark.reader.TelemetryReader;
import org.apache.metron.profiler.spark.reader.TelemetryReaders;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.apache.metron.profiler.spark.BatchProfilerConfig.TELEMETRY_INPUT_BEGIN;
import static org.apache.metron.profiler.spark.BatchProfilerConfig.TELEMETRY_INPUT_END;
import static org.apache.metron.profiler.spark.BatchProfilerConfig.TELEMETRY_INPUT_READER;
import static org.apache.spark.sql.functions.sum;

/**
 * The 'Batch Profiler' that generates profiles by consuming data in batch from archived telemetry.
 *
 * <p>The Batch Profiler is executed in Spark.
 */
public class BatchProfiler implements Serializable {

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private TimestampParser timestampParser;
  private TelemetryReader reader;

  public BatchProfiler() {
    this.timestampParser = new TimestampParser();
  }

  /**
   * Execute the Batch Profiler.
   *
   * @param spark The spark session.
   * @param profilerProps The profiler configuration properties.
   * @param globalProperties The Stellar global properties.
   * @param readerProps The properties passed to the {@link org.apache.spark.sql.DataFrameReader}.
   * @param profiles The profile definitions.
   * @return The number of profile measurements produced.
   */
  public long run(SparkSession spark,
                  Properties profilerProps,
                  Properties globalProperties,
                  Properties readerProps,
                  ProfilerConfig profiles) {

    LOG.debug("Building {} profile(s)", profiles.getProfiles().size());
    Map<String, String> globals = Maps.fromProperties(globalProperties);
    
    // fetch the archived telemetry using the input reader
    TelemetryReader reader = TelemetryReaders.create(TELEMETRY_INPUT_READER.get(profilerProps, String.class));
    Dataset<String> telemetry = reader.read(spark, profilerProps, readerProps);
    LOG.debug("Found {} telemetry record(s)", telemetry.cache().count());

    // find all routes for each message
    Dataset<MessageRoute> routes = telemetry
            .flatMap(messageRouterFunction(profilerProps, profiles, globals), Encoders.kryo(MessageRoute.class));
    LOG.debug("Generated {} message route(s)", routes.cache().count());

    // build the profiles
    Dataset<ProfileMeasurement> measurements = routes
            .groupByKey(new GroupByPeriodFunction(profilerProps), Encoders.STRING())
            .mapGroups(new ProfileBuilderFunction(profilerProps, globals), Encoders.kryo(ProfileMeasurement.class));
    LOG.debug("Produced {} profile measurement(s)", measurements.cache().count());

    // write the profile measurements to HBase
    long count = measurements
            .mapPartitions(new HBaseWriterFunction(profilerProps), Encoders.INT())
            .agg(sum("value"))
            .head()
            .getLong(0);
    LOG.debug("{} profile measurement(s) written to HBase", count);

    return count;
  }

  /**
   * Builds the function that performs message routing.
   *
   * @param profilerProps The profiler configuration properties.
   * @param profiles The profile definitions.
   * @param globals The Stellar global properties.
   * @return A {@link MessageRouterFunction}.
   */
  private MessageRouterFunction messageRouterFunction(
          Properties profilerProps,
          ProfilerConfig profiles,
          Map<String, String> globals) {
    MessageRouterFunction routerFunction = new MessageRouterFunction(profiles, globals);

    // an optional time constraint to limit how far back to look for telemetry
    Optional<Long> beginAt = timestampParser.parse(TELEMETRY_INPUT_BEGIN.get(profilerProps, String.class));
    beginAt.ifPresent(begin -> routerFunction.withBegin(begin));

    // an optional time constraint to limit the most recent telemetry
    Optional<Long> endAt = timestampParser.parse(TELEMETRY_INPUT_END.get(profilerProps, String.class));
    endAt.ifPresent(end -> routerFunction.withEnd(end));

    return routerFunction;
  }
}
